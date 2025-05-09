package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionReviewDTO {
  private long optionId;
  private String text;
  private String description;
  private boolean isSelected;
  private boolean isCorrect;
}
