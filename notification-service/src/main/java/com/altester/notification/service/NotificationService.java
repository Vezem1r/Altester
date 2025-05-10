package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import com.altester.notification.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;

public interface NotificationService {

  /**
   * Retrieves all unread notifications for a specific user.
   *
   * @param username The username of the user whose unread notifications are being retrieved
   * @return List of unread notification DTOs
   */
  List<NotificationDTO> getUnreadNotifications(String username);

  /**
   * Gets the count of unread notifications for a specific user.
   *
   * @param username The username of the user
   * @return The count of unread notifications
   */
  long getUnreadCount(String username);

  /**
   * Creates notifications for multiple users based on the provided request and sends them via
   * WebSocket.
   *
   * @param request The notification request containing details and target usernames
   * @return List of created notification DTOs
   */
  List<NotificationDTO> createNotifications(NotificationRequest request);

  /**
   * Marks a specific notification as read and updates the unread count for the user.
   *
   * @param notificationId The ID of the notification to mark as read
   * @return The updated notification DTO
   * @throws ResourceNotFoundException if the notification with the given ID does not exist
   */
  NotificationDTO markAsRead(Long notificationId);

  /**
   * Marks all notifications for a specific user as read and updates the unread count.
   *
   * @param username The username of the user whose notifications will be marked as read
   */
  void markAllAsRead(String username);

  /**
   * Searches for notifications with optional filtering by read status and search term.
   *
   * @param username The username of the user whose notifications are being searched
   * @param search The optional search term to filter notifications by title or message
   * @param read The optional read status to filter notifications
   * @param page The page number for pagination
   * @param size The page size for pagination
   * @return A page of notification DTOs matching the search criteria
   */
  Page<NotificationDTO> searchNotifications(
      String username, String search, Boolean read, int page, int size);
}
