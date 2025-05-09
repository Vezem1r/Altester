package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import com.altester.notification.exception.ResourceNotFoundException;
import com.altester.notification.model.Notification;
import com.altester.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final WebSocketService webSocketService;

  public List<NotificationDTO> getUnreadNotifications(String username) {
    log.debug("Getting unread notifications for user: {}", username);
    return notificationRepository.findUnreadNotifications(username).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public long getUnreadCount(String username) {
    return notificationRepository.countUnreadNotifications(username);
  }

  @Transactional
  public List<NotificationDTO> createNotifications(NotificationRequest request) {
    log.info("Creating notifications for {} users", request.getUsernames().size());

    List<NotificationDTO> createdNotifications = new ArrayList<>();

    for (String username : request.getUsernames()) {
      Notification notification =
          Notification.builder()
              .username(username)
              .title(request.getTitle())
              .message(request.getMessage())
              .type(request.getType())
              .actionUrl(request.getActionUrl())
              .referenceId(request.getReferenceId())
              .read(false)
              .createdAt(LocalDateTime.now())
              .build();

      Notification savedNotification = notificationRepository.save(notification);
      NotificationDTO notificationDTO = mapToDTO(savedNotification);
      createdNotifications.add(notificationDTO);

      webSocketService.sendNotification(username, notificationDTO);

      long unreadCount = getUnreadCount(username);
      webSocketService.updateUnreadCount(username, unreadCount);

      log.info("Notification created and sent to user: {}", username);
    }

    return createdNotifications;
  }

  @Transactional
  public NotificationDTO markAsRead(Long notificationId) {
    log.debug("Marking notification as read: {}", notificationId);

    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Notification", notificationId.toString()));

    notification.setRead(true);
    Notification savedNotification = notificationRepository.save(notification);

    String username = notification.getUsername();
    long unreadCount = getUnreadCount(username);
    webSocketService.updateUnreadCount(username, unreadCount);

    log.info("Notification marked as read: {}", notificationId);
    return mapToDTO(savedNotification);
  }

  @Transactional
  public void markAllAsRead(String username) {
    notificationRepository.markAllAsRead(username);
    webSocketService.updateUnreadCount(username, 0);
    log.info("All notifications marked as read for user: {}", username);
  }

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
