package com.altester.core.dtos.core_service.attempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptResultResponse {
    private long attemptId;
    private String testTitle;
    private int score;
    private int totalScore;
    private int questionsAnswered;
    private int totalQuestions;
    private boolean completed;
}