package com.altester.core.dtos.notification_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private List<String> usernames;
    private String title;
    private String message;
    private String type;
    private String actionUrl;
    private Long referenceId;
}