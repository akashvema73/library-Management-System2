package com.library.lms.service;

import com.library.lms.entity.*;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.repository.BookCopyRepository;
import com.library.lms.repository.BookIssueRepository;
import com.library.lms.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class IssueService {

    @Autowired
    private BookIssueRepository bookIssueRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private FineService fineService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private NotificationService notificationService;

    @Value("${library.issue.default-days}")
    private int defaultIssueDays;

    /**
     * Student requests a book. Creates a REQUESTED record; a librarian must approve it.
     */
    @Transactional
    public BookIssue requestIssue(User student, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        boolean alreadyHasActiveRequest = bookIssueRepository
                .findByStudentAndStatusIn(student, Arrays.asList(IssueStatus.REQUESTED, IssueStatus.ISSUED))
                .stream()
                .anyMatch(i -> i.getBook().getId().equals(bookId));

        if (alreadyHasActiveRequest) {
            throw new BadRequestException("You already have an active request or issue for this book");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No copies available. Please reserve the book instead.");
        }

        BookIssue issue = new BookIssue();
        issue.setStudent(student);
        issue.setBook(book);
        issue.setRequestDate(LocalDate.now());
        issue.setStatus(IssueStatus.REQUESTED);

        return bookIssueRepository.save(issue);
    }

    /**
     * Librarian approves a request: allocates a copy, sets issue/due dates.
     */
    @Transactional
    public BookIssue approveIssue(Long issueId, User librarian) {
        BookIssue issue = getById(issueId);
        if (issue.getStatus() != IssueStatus.REQUESTED) {
            throw new BadRequestException("Only requested issues can be approved");
        }

        Book book = issue.getBook();
        BookCopy copy = bookCopyRepository.findFirstByBookAndStatus(book, CopyStatus.AVAILABLE)
                .orElseThrow(() -> new BadRequestException("No available copies to issue"));

        copy.setStatus(CopyStatus.ISSUED);
        bookCopyRepository.save(copy);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        issue.setBookCopy(copy);
        issue.setIssuedBy(librarian);
        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(defaultIssueDays));
        issue.setStatus(IssueStatus.ISSUED);

        BookIssue saved = bookIssueRepository.save(issue);

        notificationService.notify(issue.getStudent(),
                "Your request for \"" + book.getTitle() + "\" has been approved. Due date: " + issue.getDueDate(),
                "BOOK_ISSUED");

        return saved;
    }

    @Transactional
    public BookIssue rejectIssue(Long issueId) {
        BookIssue issue = getById(issueId);
        if (issue.getStatus() != IssueStatus.REQUESTED) {
            throw new BadRequestException("Only requested issues can be rejected");
        }
        issue.setStatus(IssueStatus.REJECTED);
        BookIssue saved = bookIssueRepository.save(issue);

        notificationService.notify(issue.getStudent(),
                "Your request for \"" + issue.getBook().getTitle() + "\" was rejected.",
                "REQUEST_REJECTED");

        return saved;
    }

    /**
     * Librarian directly issues a book to a student without a prior request (walk-in issue).
     */
    @Transactional
    public BookIssue directIssue(User student, Long bookId, User librarian) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        BookCopy copy = bookCopyRepository.findFirstByBookAndStatus(book, CopyStatus.AVAILABLE)
                .orElseThrow(() -> new BadRequestException("No available copies to issue"));

        copy.setStatus(CopyStatus.ISSUED);
        bookCopyRepository.save(copy);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        BookIssue issue = new BookIssue();
        issue.setStudent(student);
        issue.setBook(book);
        issue.setBookCopy(copy);
        issue.setIssuedBy(librarian);
        issue.setRequestDate(LocalDate.now());
        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(defaultIssueDays));
        issue.setStatus(IssueStatus.ISSUED);

        BookIssue saved = bookIssueRepository.save(issue);

        notificationService.notify(student,
                "\"" + book.getTitle() + "\" has been issued to you. Due date: " + issue.getDueDate(),
                "BOOK_ISSUED");

        return saved;
    }

    /**
     * Returns a book, calculates fine if late, frees up the copy, and notifies the next
     * person in the reservation queue if any.
     */
    @Transactional
    public BookIssue returnBook(Long issueId) {
        BookIssue issue = getById(issueId);
        if (issue.getStatus() != IssueStatus.ISSUED && issue.getStatus() != IssueStatus.OVERDUE) {
            throw new BadRequestException("This book is not currently issued");
        }

        issue.setReturnDate(LocalDate.now());
        issue.setStatus(IssueStatus.RETURNED);

        double fine = fineService.calculateAndCreateFine(issue);
        issue.setFineAmount(fine);

        BookCopy copy = issue.getBookCopy();
        if (copy != null) {
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
        }

        Book book = issue.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        BookIssue saved = bookIssueRepository.save(issue);

        if (fine > 0) {
            notificationService.notify(issue.getStudent(),
                    "Book \"" + book.getTitle() + "\" returned late. Fine generated: \u20b9" + fine,
                    "FINE_GENERATED");
        }

        reservationService.notifyNextInQueue(book);

        return saved;
    }

    public BookIssue getById(Long id) {
        return bookIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue record not found with id: " + id));
    }

    public List<BookIssue> getForStudent(User student) {
        return bookIssueRepository.findByStudent(student);
    }

    public List<BookIssue> getActiveForStudent(User student) {
        return bookIssueRepository.findByStudentAndStatusIn(student,
                Arrays.asList(IssueStatus.ISSUED, IssueStatus.OVERDUE));
    }

    public List<BookIssue> getRequests() {
        return bookIssueRepository.findByStatus(IssueStatus.REQUESTED);
    }

    public List<BookIssue> getIssued() {
        return bookIssueRepository.findByStatusIn(Arrays.asList(IssueStatus.ISSUED, IssueStatus.OVERDUE));
    }

    public List<BookIssue> getOverdue() {
        return bookIssueRepository.findOverdueIssues(LocalDate.now());
    }

    public List<BookIssue> getAll() {
        return bookIssueRepository.findAll();
    }

    /**
     * Marks any ISSUED book past its due date as OVERDUE. Called on-demand from dashboard/report
     * endpoints so overdue status stays fresh without needing a scheduled job.
     */
    @Transactional
    public void refreshOverdueStatuses() {
        List<BookIssue> overdue = bookIssueRepository.findOverdueIssues(LocalDate.now());
        for (BookIssue issue : overdue) {
            if (issue.getStatus() == IssueStatus.ISSUED) {
                issue.setStatus(IssueStatus.OVERDUE);
                bookIssueRepository.save(issue);
            }
        }
    }
}
