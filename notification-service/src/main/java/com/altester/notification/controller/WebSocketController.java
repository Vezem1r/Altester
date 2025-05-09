package com.altester.notification.controller;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.exception.AuthenticationException;
import com.altester.notification.service.NotificationService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

  private final NotificationService notificationService;

  @MessageMapping("/notifications.connect")
  @SendToUser("/queue/notifications")
  public Map<String, Object> handleConnection(SimpMessageHeaderAccessor headerAccessor) {
    Principal user = headerAccessor.getUser();
    if (user == null) {
      log.error("No authenticated user found in message headers");
      throw new AuthenticationException("Authentication required");
    }

    String username = user.getName();
    log.info("WebSocket connection established for user: {}", username);

    List<NotificationDTO> unreadNotifications =
        notificationService.getUnreadNotifications(username);
    long unreadCount = notificationService.getUnreadCount(username);

    log.info(
        "Sending initial data to user {}: {} unread notifications",
        username,
        unreadNotifications.size());

    return Map.of(
        "type", "INITIAL_DATA",
        "unreadNotifications", unreadNotifications,
        "unreadCount", unreadCount);
  }
}
