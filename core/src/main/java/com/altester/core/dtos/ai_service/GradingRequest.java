package com.altester.core.dtos.ai_service;

import com.altester.core.model.ApiKey.enums.AiServiceName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingRequest {
    private Long attemptId;
    private String apiKey;
    private AiServiceName aiServiceName;
    private Long promptId;
}
