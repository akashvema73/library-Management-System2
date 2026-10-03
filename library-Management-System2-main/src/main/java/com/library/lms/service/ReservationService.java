package com.library.lms.service;

import com.library.lms.entity.Book;
import com.library.lms.entity.Reservation;
import com.library.lms.entity.ReservationStatus;
import com.library.lms.entity.User;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private NotificationService notificationService;

    public Reservation reserve(User student, Long bookId) {
        Book book = bookService.getById(bookId);

        if (book.getAvailableCopies() > 0) {
            throw new BadRequestException("This book is currently available, please request an issue instead of reserving it");
        }

        if (reservationRepository.existsByStudentAndBookAndStatus(student, book, ReservationStatus.PENDING)) {
            throw new BadRequestException("You have already reserved this book");
        }

        long queueSize = reservationRepository.countByBookAndStatus(book, ReservationStatus.PENDING);

        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setQueuePosition((int) queueSize + 1);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getForStudent(User student) {
        return reservationRepository.findByStudent(student);
    }

    public List<Reservation> getForBook(Book book) {
        return reservationRepository.findByBookAndStatusOrderByQueuePositionAsc(book, ReservationStatus.PENDING);
    }

    public void cancel(Long reservationId, User student) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (!reservation.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("You can only cancel your own reservation");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        reshuffleQueue(reservation.getBook());
    }

    /**
     * Called when a copy of a book becomes available (after a return).
     * Notifies the next student in the reservation queue.
     */
    public void notifyNextInQueue(Book book) {
        List<Reservation> pending = getForBook(book);
        Optional<Reservation> next = pending.stream()
                .filter(r -> r.getQueuePosition() != null)
                .min((a, b) -> a.getQueuePosition() - b.getQueuePosition());

        if (next.isPresent()) {
            Reservation reservation = next.get();
            reservation.setStatus(ReservationStatus.AVAILABLE);
            reservationRepository.save(reservation);
            notificationService.notify(reservation.getStudent(),
                    "Good news! \"" + book.getTitle() + "\" is now available for you to issue.",
                    "RESERVATION_AVAILABLE");
        }
    }

    private void reshuffleQueue(Book book) {
        List<Reservation> pending = getForBook(book);
        int position = 1;
        for (Reservation r : pending) {
            r.setQueuePosition(position++);
            reservationRepository.save(r);
        }
    }
}
