package com.altester.core.dtos.ai_service;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptDTO {
  private Long id;
  private String title;
  private boolean isPublic;
  private String authorUsername;
  private LocalDateTime created;
  private LocalDateTime lastModified;
}
