package com.library.lms.repository;

import com.library.lms.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
            "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
            "AND (:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) " +
            "AND (:language IS NULL OR LOWER(b.language) = LOWER(:language)) " +
            "AND (:onlyAvailable = false OR b.availableCopies > 0)")
    List<Book> searchBooks(@Param("keyword") String keyword,
                            @Param("categoryId") Long categoryId,
                            @Param("author") String author,
                            @Param("publisher") String publisher,
                            @Param("language") String language,
                            @Param("onlyAvailable") boolean onlyAvailable);

    @Query("SELECT b FROM Book b ORDER BY b.title ASC")
    List<Book> findAllOrderByTitle();
}
