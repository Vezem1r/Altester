package com.altester.core.dtos.core_service.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttemptGroup {
  private String username;
  private String firstName;
  private String lastName;
  private Integer attemptCount;
  private Double averageScore;
  private Double averageAiScore;
}
