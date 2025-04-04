package com.altester.core.dtos.core_service.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTestAttemptDTO {
    private Long testId;
    private String testName;
    private List<AttemptInfoDTO> attempts;
}