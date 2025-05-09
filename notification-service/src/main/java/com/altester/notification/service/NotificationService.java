package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import java.util.List;
import org.springframework.data.domain.Page;

public interface NotificationService {

  List<NotificationDTO> getUnreadNotifications(String username);

  long getUnreadCount(String username);

  List<NotificationDTO> createNotifications(NotificationRequest request);

  NotificationDTO markAsRead(Long notificationId);

  void markAllAsRead(String username);

  Page<NotificationDTO> searchNotifications(
      String username, String search, Boolean read, int page, int size);
}
