package com.library.lms.repository;

import com.library.lms.entity.Book;
import com.library.lms.entity.Reservation;
import com.library.lms.entity.ReservationStatus;
import com.library.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBookAndStatusOrderByQueuePositionAsc(Book book, ReservationStatus status);

    List<Reservation> findByStudent(User student);

    long countByBookAndStatus(Book book, ReservationStatus status);

    boolean existsByStudentAndBookAndStatus(User student, Book book, ReservationStatus status);
}
