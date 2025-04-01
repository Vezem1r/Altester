package com.altester.core.dtos.core_service.attempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionRequest {
    private long attemptId;
    private int currentQuestionNumber;
    private AnswerDTO currentAnswer;
}