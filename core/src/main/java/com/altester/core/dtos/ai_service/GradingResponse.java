package com.altester.core.dtos.ai_service;

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
    private Integer attemptScore;
    private List<SubmissionGradingResult> results;
}