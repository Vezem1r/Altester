package com.altester.core.dtos.core_service.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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