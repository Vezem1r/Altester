package com.altester.notification.service;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import com.altester.notification.model.Notification;
import com.altester.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;

    @Transactional
    public List<NotificationDTO> createNotifications(NotificationRequest request) {
        List<Notification> notifications = request.getUsernames().stream()
                .map(username -> Notification.builder()
                        .username(username)
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType())
                        .actionUrl(request.getActionUrl())
                        .referenceId(request.getReferenceId())
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

        savedNotifications.forEach(notification ->
                webSocketService.sendNotification(notification.getUsername(), mapToDTO(notification)));

        return savedNotifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(String username) {
        return notificationRepository.findByUsernameAndReadOrderByCreatedAtDesc(username, false).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByUsernameAndRead(username, false);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);

        return mapToDTO(savedNotification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = notificationRepository.findByUsernameAndReadOrderByCreatedAtDesc(username, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);

        webSocketService.sendUnreadCount(username, 0);
    }

    public Page<NotificationDTO> searchNotifications(String username, String searchTerm, Boolean read, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            if (read != null) {
                return notificationRepository.findByUsernameAndReadOrderByCreatedAtDesc(username, read, pageable)
                        .map(this::mapToDTO);
            } else {
                return notificationRepository.findByUsernameOrderByCreatedAtDesc(username, pageable)
                        .map(this::mapToDTO);
            }
        } else {
            if (read != null) {
                return notificationRepository.searchByUsernameAndReadAndTerm(username, read, searchTerm.trim(), pageable)
                        .map(this::mapToDTO);
            } else {
                return notificationRepository.searchByUsernameAndTerm(username, searchTerm.trim(), pageable)
                        .map(this::mapToDTO);
            }
        }
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