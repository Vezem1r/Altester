package com.altester.core.dtos.core_service.attempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private long questionId;
    private List<Long> selectedOptionIds;
    private String answerText;
}
