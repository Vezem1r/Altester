package com.altester.notification.controller;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import com.altester.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.getName()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(principal.getName()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<List<NotificationDTO>> createNotifications(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotifications(request));
    }
}