package com.altester.core.dtos.core_service.apiKey;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestApiKeyAssignmentRequest {

  @NotNull(message = "Test ID is required")
  private Long testId;

  @NotNull(message = "API key ID is required")
  private Long apiKeyId;

  private Long groupId;
}
