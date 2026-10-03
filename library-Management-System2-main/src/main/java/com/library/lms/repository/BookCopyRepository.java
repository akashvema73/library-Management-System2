package com.library.lms.repository;

import com.library.lms.entity.Book;
import com.library.lms.entity.BookCopy;
import com.library.lms.entity.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBook(Book book);

    Optional<BookCopy> findFirstByBookAndStatus(Book book, CopyStatus status);

    long countByBook(Book book);

    long countByBookAndStatus(Book book, CopyStatus status);
}
