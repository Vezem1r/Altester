package com.altester.core.dtos.core_service.review;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptReviewSubmissionDTO {
  private List<QuestionReviewSubmissionDTO> questionReviews;
}
