package com.altester.core.dtos.core_service.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupSummaryDTO {
  private Long id;
  private String name;
  private Boolean aiEvaluationEnabled;
}
