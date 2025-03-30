package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO {
    private Long id;
    private String title;
    private int duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxAttempts;
    private Integer remainingAttempts;
    private Integer totalScore;
    private Integer bestScore;
}