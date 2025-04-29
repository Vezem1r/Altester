package com.altester.core.dtos.core_service.apiKey;

import com.altester.core.model.ApiKey.enums.AiServiceName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "API key is required")
    private String apiKey;

    @NotBlank(message = "AI service name is required")
    private AiServiceName aiServiceName;

    @NotNull(message = "Global flag is required")
    private Boolean isGlobal;

    private String description;
}
