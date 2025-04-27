package com.altester.core.dtos.core_service.apiKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyAssignmentDTO {
    private Long apiKeyId;
    private String apiKeyName;
    private String maskedKey;
    private String aiServiceName;
    private GroupTeacherDTO group;
    private boolean aiEvaluationEnabled;
}
