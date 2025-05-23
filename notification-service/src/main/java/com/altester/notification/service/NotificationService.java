package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
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
