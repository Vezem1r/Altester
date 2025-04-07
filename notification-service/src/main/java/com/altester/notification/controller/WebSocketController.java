package com.altester.notification.controller;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final NotificationService notificationService;

    @MessageMapping("/notifications.connect")
    @SendToUser("/queue/notifications.connection")
    public Map<String, Object> handleConnection(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            log.error("No authenticated user found in message headers");
            throw new SecurityException("Authentication required");
        }

        String username = user.getName();
        log.debug("Handling connection for user: {}", username);

        List<NotificationDTO> unreadNotifications = notificationService.getUnreadNotifications(username);
        long unreadCount = notificationService.getUnreadCount(username);

        return Map.of(
                "unreadNotifications", unreadNotifications,
                "unreadCount", unreadCount
        );
    }

    @MessageMapping("/notifications.acknowledge")
    @SendToUser("/queue/notifications.ack")
    public NotificationDTO acknowledgeNotification(
            @Payload NotificationDTO notificationDTO) {
        return notificationDTO;
    }
}