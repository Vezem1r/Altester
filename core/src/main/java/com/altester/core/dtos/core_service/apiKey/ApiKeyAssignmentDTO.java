package com.altester.core.dtos.core_service.apiKey;

import com.altester.core.model.ApiKey.enums.AiServiceName;
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
    private AiServiceName aiServiceName;
    private String model;
    private String promptName;
    private GroupTeacherDTO group;
    private boolean aiEvaluationEnabled;
}
