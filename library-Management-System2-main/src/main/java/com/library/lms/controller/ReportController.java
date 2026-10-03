package com.library.lms.controller;

import com.library.lms.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(reportService.getSummary());
    }

    @GetMapping("/most-borrowed-books")
    public ResponseEntity<List<Map<String, Object>>> mostBorrowedBooks() {
        return ResponseEntity.ok(reportService.getMostBorrowedBooks());
    }

    @GetMapping("/most-active-students")
    public ResponseEntity<List<Map<String, Object>>> mostActiveStudents() {
        return ResponseEntity.ok(reportService.getMostActiveStudents());
    }

    @GetMapping("/category-wise")
    public ResponseEntity<List<Map<String, Object>>> categoryWise() {
        return ResponseEntity.ok(reportService.getCategoryWiseBorrowing());
    }

    @GetMapping("/department-wise")
    public ResponseEntity<List<Map<String, Object>>> departmentWise() {
        return ResponseEntity.ok(reportService.getDepartmentWiseBorrowing());
    }
}
