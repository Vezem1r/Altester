package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptReviewDTO {
    private long attemptId;
    private String testTitle;
    private String testDescription;
    private int score;
    private int totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionReviewDTO> questions;
}