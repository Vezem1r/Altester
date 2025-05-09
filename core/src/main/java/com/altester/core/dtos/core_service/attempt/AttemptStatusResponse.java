package com.altester.core.dtos.core_service.attempt;

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
public class AttemptStatusResponse {
  private long attemptId;
  private String testTitle;
  private int duration;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private boolean isActive;
  private boolean isCompleted;
  private boolean isExpired;
  private int timeRemainingSeconds;
  private int totalQuestions;
  private int answeredQuestions;
  private List<QuestionAnswerStatus> questionStatuses;
  private int lastQuestionViewed;
}
