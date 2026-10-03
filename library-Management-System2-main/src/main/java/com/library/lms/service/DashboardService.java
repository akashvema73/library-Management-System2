package com.library.lms.service;

import com.library.lms.entity.IssueStatus;
import com.library.lms.entity.Role;
import com.library.lms.entity.User;
import com.library.lms.repository.BookIssueRepository;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookIssueRepository bookIssueRepository;

    @Autowired
    private IssueService issueService;

    @Autowired
    private FineService fineService;

    public Map<String, Object> getAdminStats() {
        issueService.refreshOverdueStatuses();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalStudents", userRepository.countByRole(Role.STUDENT));
        stats.put("totalLibrarians", userRepository.countByRole(Role.LIBRARIAN));
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalCopies", bookRepository.findAll().stream().mapToInt(b -> b.getTotalCopies() == null ? 0 : b.getTotalCopies()).sum());
        stats.put("booksIssued", bookIssueRepository.countByStatus(IssueStatus.ISSUED) + bookIssueRepository.countByStatus(IssueStatus.OVERDUE));
        stats.put("overdueBooks", bookIssueRepository.countOverdue(LocalDate.now()));
        stats.put("pendingRequests", bookIssueRepository.countByStatus(IssueStatus.REQUESTED));
        stats.put("totalFineCollected", fineService.getTotalCollected());
        return stats;
    }

    public Map<String, Object> getLibrarianStats() {
        issueService.refreshOverdueStatuses();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalStudents", userRepository.countByRole(Role.STUDENT));
        stats.put("booksIssued", bookIssueRepository.countByStatus(IssueStatus.ISSUED) + bookIssueRepository.countByStatus(IssueStatus.OVERDUE));
        stats.put("overdueBooks", bookIssueRepository.countOverdue(LocalDate.now()));
        stats.put("pendingRequests", bookIssueRepository.countByStatus(IssueStatus.REQUESTED));
        stats.put("collectedFine", fineService.getTotalCollected());
        return stats;
    }

    public Map<String, Object> getStudentStats(User student) {
        issueService.refreshOverdueStatuses();
        Map<String, Object> stats = new LinkedHashMap<>();
        long issuedCount = issueService.getActiveForStudent(student).size();
        long dueSoon = issueService.getActiveForStudent(student).stream()
                .filter(i -> i.getDueDate() != null &&
                        !i.getDueDate().isBefore(LocalDate.now()) &&
                        i.getDueDate().isBefore(LocalDate.now().plusDays(3)))
                .count();
        stats.put("booksIssued", issuedCount);
        stats.put("dueSoon", dueSoon);
        stats.put("pendingFine", fineService.getPendingTotalForStudent(student));
        return stats;
    }
}
