package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.AttemptStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptReviewDTO {
  private long attemptId;
  private String testTitle;
  private String testDescription;
  private AttemptStatus status;
  private int score;
  private int aiScore;
  private int totalScore;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private List<QuestionReviewDTO> questions;
}
