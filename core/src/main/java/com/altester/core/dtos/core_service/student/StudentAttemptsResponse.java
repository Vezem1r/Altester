package com.altester.core.dtos.core_service.student;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttemptsResponse {
  private long testId;
  private String testTitle;
  private int totalScore;
  private List<TestAttemptDTO> attempts;
}
