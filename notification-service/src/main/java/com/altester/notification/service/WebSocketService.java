package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String username, NotificationDTO notification) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }

    public void sendUnreadCount(String username, long count) {
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications/count",
                response
        );
    }
}