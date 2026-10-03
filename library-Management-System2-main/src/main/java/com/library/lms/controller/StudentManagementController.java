package com.library.lms.controller;

import com.library.lms.entity.User;
import com.library.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Accessible to librarians (and admins, via the shared /api/librarian/** authorization rule)
 * to look up student records while issuing/returning books.
 */
@RestController
@RequestMapping("/api/librarian/students")
public class StudentManagementController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.searchStudents(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
