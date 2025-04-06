package com.altester.notification.repository;

import com.altester.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);
    List<Notification> findByUsernameAndReadOrderByCreatedAtDesc(String username, boolean read);
    long countByUsernameAndRead(String username, boolean read);
}