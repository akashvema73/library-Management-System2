package com.library.lms.controller;

import com.library.lms.entity.Notification;
import com.library.lms.entity.User;
import com.library.lms.security.AuthUtil;
import com.library.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<List<Notification>> myNotifications() {
        User user = authUtil.getCurrentUser();
        return ResponseEntity.ok(notificationService.getForUser(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        User user = authUtil.getCurrentUser();
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(user)));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        User user = authUtil.getCurrentUser();
        notificationService.markAllRead(user);
        return ResponseEntity.noContent().build();
    }
}
