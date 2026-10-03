package com.library.lms.repository;

import com.library.lms.entity.Book;
import com.library.lms.entity.BookIssue;
import com.library.lms.entity.IssueStatus;
import com.library.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookIssueRepository extends JpaRepository<BookIssue, Long> {

    List<BookIssue> findByStudent(User student);

    List<BookIssue> findByStudentAndStatusIn(User student, List<IssueStatus> statuses);

    List<BookIssue> findByStatus(IssueStatus status);

    List<BookIssue> findByStatusIn(List<IssueStatus> statuses);

    long countByStatus(IssueStatus status);

    @Query("SELECT COUNT(bi) FROM BookIssue bi WHERE bi.status = 'ISSUED' AND bi.dueDate < :today")
    long countOverdue(@Param("today") LocalDate today);

    @Query("SELECT bi FROM BookIssue bi WHERE bi.status = 'ISSUED' AND bi.dueDate < :today")
    List<BookIssue> findOverdueIssues(@Param("today") LocalDate today);

    @Query("SELECT bi.book.title as title, COUNT(bi) as cnt FROM BookIssue bi " +
            "WHERE bi.status IN ('ISSUED','RETURNED') GROUP BY bi.book.title ORDER BY cnt DESC")
    List<Object[]> findMostBorrowedBooks();

    @Query("SELECT bi.student.name as name, COUNT(bi) as cnt FROM BookIssue bi " +
            "WHERE bi.status IN ('ISSUED','RETURNED') GROUP BY bi.student.name ORDER BY cnt DESC")
    List<Object[]> findMostActiveStudents();

    @Query("SELECT bi.book.category.name as category, COUNT(bi) as cnt FROM BookIssue bi " +
            "WHERE bi.status IN ('ISSUED','RETURNED') GROUP BY bi.book.category.name ORDER BY cnt DESC")
    List<Object[]> findCategoryWiseBorrowing();

    @Query("SELECT bi.student.department as dept, COUNT(bi) as cnt FROM BookIssue bi " +
            "WHERE bi.status IN ('ISSUED','RETURNED') GROUP BY bi.student.department ORDER BY cnt DESC")
    List<Object[]> findDepartmentWiseBorrowing();

    List<BookIssue> findByBook(Book book);
}
