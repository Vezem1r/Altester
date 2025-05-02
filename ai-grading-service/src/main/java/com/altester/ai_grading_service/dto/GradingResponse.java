package com.altester.ai_grading_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResponse {

    private Long attemptId;
    private boolean success;
    private String message;
    private List<SubmissionGradingResult> results;
}
