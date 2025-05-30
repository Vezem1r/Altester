package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.AttemptStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO {
  private Long id;
  private String title;
  private int duration;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer maxAttempts;
  private Integer remainingAttempts;
  private Integer totalScore;
  private Integer bestScore;
  private int numberOfQuestions;
  private AttemptStatus status;
}
