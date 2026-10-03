package com.library.lms.controller;

import com.library.lms.dto.DashboardStatsResponse;
import com.library.lms.entity.User;
import com.library.lms.security.AuthUtil;
import com.library.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> adminStats() {
        return ResponseEntity.ok(new DashboardStatsResponse(dashboardService.getAdminStats()));
    }

    @GetMapping("/librarian")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<DashboardStatsResponse> librarianStats() {
        return ResponseEntity.ok(new DashboardStatsResponse(dashboardService.getLibrarianStats()));
    }

    @GetMapping("/student")
    public ResponseEntity<DashboardStatsResponse> studentStats() {
        User student = authUtil.getCurrentUser();
        return ResponseEntity.ok(new DashboardStatsResponse(dashboardService.getStudentStats(student)));
    }
}
