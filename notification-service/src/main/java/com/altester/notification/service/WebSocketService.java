package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String username, NotificationDTO notification) {
        log.info("Sending {} notification to user: {}",notification.getType(), username);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }

    public void sendUnreadCount(String username, long count) {
        log.info("Sending count: {} of unread notification to user: {}", count, username);
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications/count",
                response
        );
    }
}