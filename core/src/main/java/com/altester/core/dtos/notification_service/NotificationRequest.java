package com.altester.core.dtos.notification_service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
