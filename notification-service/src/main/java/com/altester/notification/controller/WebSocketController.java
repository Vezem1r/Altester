package com.altester.notification.controller;

import com.altester.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    @MessageMapping("/notifications.connect")
    @SendToUser("/queue/notifications.connection")
    public String handleConnection() {
        return "Connected to notifications WebSocket";
    }

    @MessageMapping("/notifications.acknowledge")
    @SendToUser("/queue/notifications.ack")
    public NotificationDTO acknowledgeNotification(@Payload NotificationDTO notificationDTO) {
        return notificationDTO;
    }
}