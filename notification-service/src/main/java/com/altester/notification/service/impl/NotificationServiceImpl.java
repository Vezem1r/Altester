package com.altester.notification.service.impl;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.model.Notification;
import com.altester.notification.repository.NotificationRepository;
import com.altester.notification.service.NotificationService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public List<NotificationDTO> getUnreadNotifications(String username) {
    log.debug("Getting unread notifications for user: {}", username);
    return notificationRepository.findUnreadNotifications(username).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public long getUnreadCount(String username) {
    return notificationRepository.countUnreadNotifications(username);
  }

  @Override
  public Page<NotificationDTO> searchNotifications(
      String username, String search, Boolean read, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Notification> notificationsPage;

    if (search != null && !search.isBlank()) {
      if (read != null) {
        notificationsPage =
            notificationRepository.findByUsernameAndReadAndSearchTerm(
                username, read, search, pageRequest);
      } else {
        notificationsPage =
            notificationRepository.findByUsernameAndSearchTerm(username, search, pageRequest);
      }
    } else {
      if (read != null) {
        notificationsPage =
            notificationRepository.findByUsernameAndRead(username, read, pageRequest);
      } else {
        notificationsPage = notificationRepository.findByUsername(username, pageRequest);
      }
    }

    return notificationsPage.map(this::mapToDTO);
  }

  private NotificationDTO mapToDTO(Notification notification) {
    return NotificationDTO.builder()
        .id(notification.getId())
        .username(notification.getUsername())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .read(notification.isRead())
        .type(notification.getType())
        .actionUrl(notification.getActionUrl())
        .referenceId(notification.getReferenceId())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
