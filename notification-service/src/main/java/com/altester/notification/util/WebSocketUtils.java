package com.altester.notification.util;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationMessageType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketUtils {

  private WebSocketUtils() {}

  /**
   * Creates a basic response with just the message type
   *
   * @param type The notification message type
   * @return A map containing the message type
   */
  public static Map<String, Object> createResponse(NotificationMessageType type) {
    Map<String, Object> response = new HashMap<>();
    response.put("type", type.toString());
    return response;
  }

  /**
   * Creates a response for initial data when a user connects
   *
   * @param type The notification message type (should be INITIAL_DATA)
   * @param notifications List of unread notifications
   * @param count Count of unread notifications
   * @return A map containing the initial data response
   */
  public static Map<String, Object> createInitialDataResponse(
      NotificationMessageType type, List<NotificationDTO> notifications, long count) {
    Map<String, Object> response = createResponse(type);
    response.put("unreadNotifications", notifications);
    response.put("unreadCount", count);
    return response;
  }
}
