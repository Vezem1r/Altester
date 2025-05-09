package com.altester.core.dtos.core_service.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReviewSubmissionDTO {
  private Long questionId;
  private Integer score;
  private String teacherFeedback;
}
