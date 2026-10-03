package com.library.lms.controller;

import com.library.lms.dto.RegisterRequest;
import com.library.lms.entity.AccountStatus;
import com.library.lms.entity.Role;
import com.library.lms.entity.User;
import com.library.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/librarians")
    public ResponseEntity<User> createLibrarian(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.createUserByAdmin(request, Role.LIBRARIAN));
    }

    @GetMapping("/librarians")
    public ResponseEntity<List<User>> getLibrarians() {
        return ResponseEntity.ok(userService.getLibrarians());
    }

    @PostMapping("/students")
    public ResponseEntity<User> createStudent(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.createUserByAdmin(request, Role.STUDENT));
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getStudents(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.searchStudents(keyword));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<User> updateStatus(@PathVariable Long id, @RequestParam AccountStatus status) {
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
