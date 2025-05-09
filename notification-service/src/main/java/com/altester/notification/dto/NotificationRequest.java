package com.altester.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
  @NotEmpty private List<String> usernames;
  @NotNull private String title;
  @NotNull private String message;
  private String type;
  private String actionUrl;
  private Long referenceId;
}
