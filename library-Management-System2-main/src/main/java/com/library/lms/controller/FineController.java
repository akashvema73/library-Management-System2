package com.library.lms.controller;

import com.library.lms.entity.Fine;
import com.library.lms.entity.User;
import com.library.lms.security.AuthUtil;
import com.library.lms.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FineController {

    @Autowired
    private FineService fineService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/api/student/fines")
    public ResponseEntity<List<Fine>> myFines() {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(fineService.getForStudent(student));
    }

    @PostMapping("/api/student/fines/{id}/pay")
    public ResponseEntity<Fine> pay(@PathVariable Long id) {
        User requester = authUtil.getCurrentUser();
        return ResponseEntity.ok(fineService.pay(id, requester));
    }

    @GetMapping("/api/librarian/fines")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<Fine>> allFines() {
        return ResponseEntity.ok(fineService.getAll());
    }

    @GetMapping("/api/librarian/fines/pending")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<Fine>> pendingFines() {
        return ResponseEntity.ok(fineService.getPending());
    }

    @PostMapping("/api/librarian/fines/{id}/waive")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<Fine> waive(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.waive(id));
    }

    @PostMapping("/api/librarian/fines/{id}/collect")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<Fine> collectInPerson(@PathVariable Long id) {
        User requester = authUtil.getCurrentUser();
        return ResponseEntity.ok(fineService.pay(id, requester));
    }
}
