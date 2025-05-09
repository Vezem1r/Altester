package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.AttemptStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptDTO {
  private long attemptId;
  private int attemptNumber;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private AttemptStatus status;
  private Integer score;
  private int answeredQuestions;
  private int totalQuestions;
}
