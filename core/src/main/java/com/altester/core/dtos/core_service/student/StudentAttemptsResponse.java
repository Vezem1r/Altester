package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttemptsResponse {
    private long testId;
    private String testTitle;
    private int totalScore;
    private List<TestAttemptDTO> attempts;
}