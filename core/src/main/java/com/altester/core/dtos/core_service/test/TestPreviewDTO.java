package com.altester.core.dtos.core_service.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPreviewDTO {
    private Long id;
    private String title;
    private String description;
    private int duration;
    private boolean isOpen;
    private Integer maxAttempts;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isCreatedByAdmin;
    private Boolean allowTeacherEdit;
    private Boolean AiEvaluate;
    private int totalQuestions;
    private int totalScore;
    private Integer maxQuestions;
    private List<GroupSummaryDTO> associatedGroups;
    private List<QuestionDTO> questions;
}