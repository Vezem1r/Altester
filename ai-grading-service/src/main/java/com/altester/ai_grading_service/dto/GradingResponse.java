package com.altester.ai_grading_service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResponse {
  private Long attemptId;
  private boolean success;
  private String message;
  private Integer attemptScore;
  private List<SubmissionGradingResult> results;
}
