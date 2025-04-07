package com.altester.notification.controller;

import com.altester.notification.dto.NotificationDTO;
import com.altester.notification.dto.NotificationRequest;
import com.altester.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<List<NotificationDTO>> createNotifications(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotifications(request));
    }
}