package com.altester.notification.service;

import com.altester.notification.exception.InternalServerException;
import com.altester.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupService {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
            log.info("Initializing notifications cleanup for notifications older than {}", twoWeeksAgo);
            int deletedCount = notificationRepository.deleteByReadTrueAndCreatedAtBefore(twoWeeksAgo);
            log.info("Deleted {} old notifications", deletedCount);
        } catch (Exception e) {
            log.error("Error during notification cleanup: {}", e.getMessage(), e);
            throw new InternalServerException("Failed to cleanup old notifications", e);
        }
    }
}