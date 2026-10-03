package com.library.lms.service;

import com.library.lms.entity.Notification;
import com.library.lms.entity.User;
import com.library.lms.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void notify(User user, String message, String type) {
        Notification notification = new Notification(user, message, type);
        notificationRepository.save(notification);
    }

    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedDateDesc(user);
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAllRead(User user) {
        List<Notification> list = notificationRepository.findByUserOrderByCreatedDateDesc(user);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }
}
