package com.altester.core.dtos.core_service.apiKey;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.enums.AiServiceName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyDTO {
    private Long id;
    private String name;
    private String keyMasked;
    private AiServiceName aiServiceName;
    private String model;
    private boolean isGlobal;
    private Long ownerId;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private String description;
    private boolean isActive;
    private boolean isOwnedByCurrentUser;

    public static ApiKeyDTO fromEntity(ApiKey apiKey, Long currentUserId) {
        String maskedKey = apiKey.getKeyPrefix() + "*".repeat(8) + apiKey.getKeySuffix();

        return ApiKeyDTO.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyMasked(maskedKey)
                .aiServiceName(apiKey.getAiServiceName())
                .model(apiKey.getModel())
                .isGlobal(apiKey.isGlobal())
                .ownerId(apiKey.getOwner() != null ? apiKey.getOwner().getId() : null)
                .ownerUsername(apiKey.getOwner() != null ? apiKey.getOwner().getUsername() : "System")
                .createdAt(apiKey.getCreatedAt())
                .description(apiKey.getDescription())
                .isActive(apiKey.isActive())
                .isOwnedByCurrentUser(apiKey.getOwner() != null &&
                        apiKey.getOwner().getId().equals(currentUserId))
                .build();
    }
}
