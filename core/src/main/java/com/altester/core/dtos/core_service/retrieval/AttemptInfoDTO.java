package com.altester.core.dtos.core_service.retrieval;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptInfoDTO {
  private Long attemptId;
  private Integer attemptNumber;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer score;
  private String status;
}
