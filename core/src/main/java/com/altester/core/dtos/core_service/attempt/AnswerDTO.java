package com.altester.core.dtos.core_service.attempt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
  private long questionId;
  private List<Long> selectedOptionIds;
  private String answerText;
}
