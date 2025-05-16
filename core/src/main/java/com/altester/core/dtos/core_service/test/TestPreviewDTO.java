package com.altester.core.dtos.core_service.test;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPreviewDTO {
  private Long id;
  private String title;
  private String description;
  private int duration;
  private Integer maxAttempts;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  private boolean isOpen;
  private boolean isCreatedByAdmin;
  private Boolean allowTeacherEdit;
  private Boolean AiEvaluate;

  private int totalScore;
  private int totalQuestions;

  // Configured amount of questions in test
  private Integer easyQuestionsCount;
  private Integer mediumQuestionsCount;
  private Integer hardQuestionsCount;

  // Total amount of questions (of ech type) currently assigned to test
  private Integer easyQuestionsSetup;
  private Integer mediumQuestionsSetup;
  private Integer hardQuestionsSetup;

  // Configured score for each question type
  private Integer mediumScore;
  private Integer easyScore;
  private Integer hardScore;

  private List<GroupSummaryDTO> associatedGroups;
}
