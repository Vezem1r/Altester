package com.altester.core.dtos.ai_service;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentPromptRequest {
  private Long testId;
  private Long groupId;
  private Long promptId;
}
