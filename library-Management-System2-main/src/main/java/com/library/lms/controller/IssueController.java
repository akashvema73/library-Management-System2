package com.library.lms.controller;

import com.library.lms.dto.IssueRequestDto;
import com.library.lms.entity.BookIssue;
import com.library.lms.entity.User;
import com.library.lms.security.AuthUtil;
import com.library.lms.service.IssueService;
import com.library.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserService userService;

    // ---------- Student endpoints ----------

    @PostMapping("/api/student/issues/request")
    public ResponseEntity<BookIssue> requestIssue(@Valid @RequestBody IssueRequestDto request) {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(issueService.requestIssue(student, request.getBookId()));
    }

    @GetMapping("/api/student/issues")
    public ResponseEntity<List<BookIssue>> myIssues() {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(issueService.getForStudent(student));
    }

    @GetMapping("/api/student/issues/active")
    public ResponseEntity<List<BookIssue>> myActiveIssues() {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(issueService.getActiveForStudent(student));
    }

    // ---------- Librarian endpoints ----------

    @GetMapping("/api/librarian/issues/requests")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<BookIssue>> getRequests() {
        return ResponseEntity.ok(issueService.getRequests());
    }

    @GetMapping("/api/librarian/issues")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<BookIssue>> getIssued() {
        return ResponseEntity.ok(issueService.getIssued());
    }

    @GetMapping("/api/librarian/issues/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<BookIssue>> getOverdue() {
        return ResponseEntity.ok(issueService.getOverdue());
    }

    @GetMapping("/api/librarian/issues/all")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<List<BookIssue>> getAll() {
        return ResponseEntity.ok(issueService.getAll());
    }

    @PostMapping("/api/librarian/issues/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssue> approve(@PathVariable Long id) {
        User librarian = authUtil.getCurrentUser();
        return ResponseEntity.ok(issueService.approveIssue(id, librarian));
    }

    @PostMapping("/api/librarian/issues/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssue> reject(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.rejectIssue(id));
    }

    @PostMapping("/api/librarian/issues/direct")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssue> directIssue(@RequestParam Long studentId, @RequestParam Long bookId) {
        User librarian = authUtil.getCurrentUser();
        User student = userService.getById(studentId);
        return ResponseEntity.ok(issueService.directIssue(student, bookId, librarian));
    }

    @PostMapping("/api/librarian/issues/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssue> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.returnBook(id));
    }
}
