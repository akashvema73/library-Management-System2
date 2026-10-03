package com.library.lms.controller;

import com.library.lms.entity.Reservation;
import com.library.lms.entity.User;
import com.library.lms.security.AuthUtil;
import com.library.lms.service.BookService;
import com.library.lms.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/api/student/reservations")
    public ResponseEntity<Reservation> reserve(@RequestParam Long bookId) {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.reserve(student, bookId));
    }

    @GetMapping("/api/student/reservations")
    public ResponseEntity<List<Reservation>> myReservations() {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(reservationService.getForStudent(student));
    }

    @DeleteMapping("/api/student/reservations/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        User student = authUtil.getCurrentUser();
        reservationService.cancel(id, student);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/librarian/reservations/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<Reservation>> forBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getForBook(bookService.getById(bookId)));
    }
}
