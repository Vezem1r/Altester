package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;

public interface WebSocketService {

  /**
   * Sends a notification to a specific user via WebSocket.
   *
   * @param username The username of the recipient user
   * @param notification The notification object to be sent
   */
  void sendNotification(String username, NotificationDTO notification);

  /**
   * Updates and sends the unread notifications count to a specific user via WebSocket.
   *
   * @param username The username of the recipient user
   * @param count The current count of unread notifications
   */
  void updateUnreadCount(String username, long count);
}
