package com.library.lms.service;

import com.library.lms.dto.BookRequest;
import com.library.lms.entity.Book;
import com.library.lms.entity.BookCopy;
import com.library.lms.entity.BookStatus;
import com.library.lms.entity.Category;
import com.library.lms.entity.CopyStatus;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.repository.BookCopyRepository;
import com.library.lms.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private CategoryService categoryService;

    public List<Book> getAll() {
        return bookRepository.findAllOrderByTitle();
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public List<Book> search(String keyword, Long categoryId, String author, String publisher,
                              String language, boolean onlyAvailable) {
        return bookRepository.searchBooks(
                blankToNull(keyword), categoryId, blankToNull(author),
                blankToNull(publisher), blankToNull(language), onlyAvailable);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public Book create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("A book with this ISBN already exists");
        }
        Category category = categoryService.getById(request.getCategoryId());

        Book book = new Book();
        mapRequestToBook(book, request, category);
        book.setAvailableCopies(request.getTotalCopies());
        book.setStatus(BookStatus.ACTIVE);
        Book saved = bookRepository.save(book);

        generateCopies(saved, request.getTotalCopies());
        return saved;
    }

    private void mapRequestToBook(Book book, BookRequest request, Category category) {
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setCategory(category);
        book.setEdition(request.getEdition());
        book.setPublicationYear(request.getPublicationYear());
        book.setLanguage(request.getLanguage());
        book.setPrice(request.getPrice());
        book.setShelfNumber(request.getShelfNumber());
        book.setRackNumber(request.getRackNumber());
        book.setTotalCopies(request.getTotalCopies());
    }

    private void generateCopies(Book book, int count) {
        for (int i = 1; i <= count; i++) {
            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setCopyCode(String.format("BK%d-COPY-%03d", book.getId(), i));
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
        }
    }

    public Book update(Long id, BookRequest request) {
        Book book = getById(id);
        Category category = categoryService.getById(request.getCategoryId());

        long existingCopies = bookCopyRepository.countByBook(book);
        int newTotal = request.getTotalCopies();

        mapRequestToBook(book, request, category);

        if (newTotal > existingCopies) {
            long toAdd = newTotal - existingCopies;
            book.setAvailableCopies(book.getAvailableCopies() + (int) toAdd);
            Book saved = bookRepository.save(book);
            for (int i = 0; i < toAdd; i++) {
                BookCopy copy = new BookCopy();
                copy.setBook(saved);
                copy.setCopyCode(String.format("BK%d-COPY-%03d", saved.getId(), existingCopies + i + 1));
                copy.setStatus(CopyStatus.AVAILABLE);
                bookCopyRepository.save(copy);
            }
            return saved;
        }

        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = getById(id);
        List<BookCopy> copies = bookCopyRepository.findByBook(book);
        bookCopyRepository.deleteAll(copies);
        bookRepository.delete(book);
    }

    public List<BookCopy> getCopies(Long bookId) {
        Book book = getById(bookId);
        return bookCopyRepository.findByBook(book);
    }
}
