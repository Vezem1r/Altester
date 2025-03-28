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
public class TestSummaryDTO {
    private Long id;
    private String title;
    private int duration;
    private boolean isOpen;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalScore;
    private Integer maxAttempts;
    private Boolean allowTeacherEdit;
    private List<GroupSummaryDTO> associatedGroups;
}