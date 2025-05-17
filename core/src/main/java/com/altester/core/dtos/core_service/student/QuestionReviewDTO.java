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
public class QuestionReviewDTO {
  private long submissionId;
  private String questionText;
  private String imagePath;
  private List<OptionReviewDTO> options;
  private String studentAnswer;
  private List<Long> selectedOptionIds;
  private int score;
  private int aiScore;
  private int maxScore;
  private String teacherFeedback;
  private String aiFeedback;
  private boolean isAiGraded;
  private boolean isRequested;
}
