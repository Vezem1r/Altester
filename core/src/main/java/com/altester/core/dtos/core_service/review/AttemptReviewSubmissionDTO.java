package com.altester.core.dtos.core_service.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptReviewSubmissionDTO {
    private List<QuestionReviewSubmissionDTO> questionReviews;
}