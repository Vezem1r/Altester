package com.altester.core.dtos.core_service.attempt;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleQuestionResponse {
  private long attemptId;
  private String testTitle;
  private String testDescription;
  private int duration;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private int totalQuestions;
  private int currentQuestionNumber;
  private QuestionDTO question;
  private AnswerDTO currentAnswer;
  private boolean isCompleted;
  private boolean isExpired;
  private int timeRemainingSeconds;
}
