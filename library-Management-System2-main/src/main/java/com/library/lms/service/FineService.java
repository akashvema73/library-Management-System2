package com.library.lms.service;

import com.library.lms.entity.BookIssue;
import com.library.lms.entity.Fine;
import com.library.lms.entity.FineStatus;
import com.library.lms.entity.User;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.repository.FineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FineService {

    @Autowired
    private FineRepository fineRepository;

    @Value("${library.fine.per-day}")
    private double finePerDay;

    @Value("${library.fine.max}")
    private double maxFine;

    /**
     * Calculates and, if applicable, persists a fine for a late return.
     * Returns the fine amount (0 if returned on time).
     */
    public double calculateAndCreateFine(BookIssue issue) {
        long lateDays = ChronoUnit.DAYS.between(issue.getDueDate(), issue.getReturnDate());
        if (lateDays <= 0) {
            return 0.0;
        }
        double amount = Math.min(lateDays * finePerDay, maxFine);

        Fine fine = new Fine();
        fine.setIssue(issue);
        fine.setStudent(issue.getStudent());
        fine.setAmount(amount);
        fine.setStatus(FineStatus.PENDING);
        fine.setCreatedDate(LocalDate.now());
        fineRepository.save(fine);

        return amount;
    }

    public List<Fine> getForStudent(User student) {
        return fineRepository.findByStudent(student);
    }

    public List<Fine> getAll() {
        return fineRepository.findAll();
    }

    public List<Fine> getPending() {
        return fineRepository.findByStatus(FineStatus.PENDING);
    }

    public double getPendingTotalForStudent(User student) {
        return fineRepository.findByStudentAndStatus(student, FineStatus.PENDING)
                .stream().mapToDouble(Fine::getAmount).sum();
    }

    public Fine pay(Long fineId, User requester) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found"));

        if (requester.getRole().name().equals("STUDENT") && !fine.getStudent().getId().equals(requester.getId())) {
            throw new BadRequestException("You can only pay your own fine");
        }
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new BadRequestException("This fine is not pending");
        }
        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDate.now());
        fine.getIssue().setFinePaid(true);
        return fineRepository.save(fine);
    }

    public Fine waive(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found"));
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new BadRequestException("Only pending fines can be waived");
        }
        fine.setStatus(FineStatus.WAIVED);
        return fineRepository.save(fine);
    }

    public double getTotalCollected() {
        Double total = fineRepository.sumCollectedFine();
        return total == null ? 0.0 : total;
    }
}
