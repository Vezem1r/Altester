package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationMessageType;
import com.altester.notification.util.WebSocketUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  public void sendNotification(String username, NotificationDTO notification) {
    log.info("Sending notification to user: {}", username);
    log.debug(
        "Notification details: id={}, title={}, message={}",
        notification.getId(),
        notification.getTitle(),
        notification.getMessage());

    try {
      Map<String, Object> message =
          WebSocketUtils.createNewNotificationResponse(
              NotificationMessageType.NEW_NOTIFICATION, notification);

      messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
      log.info("Notification sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending notification to {}: {}", username, e.getMessage(), e);
    }
  }

  public void updateUnreadCount(String username, long count) {
    log.info("Sending unread count update to user: {}, count: {}", username, count);

    try {
      Map<String, Object> message =
          WebSocketUtils.createUnreadCountResponse(NotificationMessageType.UNREAD_COUNT, count);

      messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
      log.info("Unread count update sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending unread count update to {}: {}", username, e.getMessage(), e);
    }
  }
}
