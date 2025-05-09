package com.altester.ai_grading_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingRequest {

  @NotNull private Long attemptId;
  @NotNull private String apiKey;
  @NotNull private String model;
  @NotNull private String aiServiceName;
  private Long promptId;
}
