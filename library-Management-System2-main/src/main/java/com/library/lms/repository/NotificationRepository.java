package com.library.lms.repository;

import com.library.lms.entity.Notification;
import com.library.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedDateDesc(User user);

    long countByUserAndIsReadFalse(User user);
}
