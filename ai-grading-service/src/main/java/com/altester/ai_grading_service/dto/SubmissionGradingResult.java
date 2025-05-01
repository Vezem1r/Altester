package com.altester.ai_grading_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionGradingResult {

    private Long submissionId;
    private Integer score;
    private String feedback;
    private boolean graded;
}
