package com.library.lms.repository;

import com.library.lms.entity.Fine;
import com.library.lms.entity.FineStatus;
import com.library.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByStudent(User student);

    List<Fine> findByStudentAndStatus(User student, FineStatus status);

    List<Fine> findByStatus(FineStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = 'PAID'")
    Double sumCollectedFine();

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.student = :student AND f.status = 'PENDING'")
    Double sumPendingFineForStudent(@Param("student") User student);
}
