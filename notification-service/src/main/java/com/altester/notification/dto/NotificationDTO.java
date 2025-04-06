package com.altester.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String username;
    private String title;
    private String message;
    private boolean read;
    private String type;
    private String actionUrl;
    private Long referenceId;
    private LocalDateTime createdAt;
}