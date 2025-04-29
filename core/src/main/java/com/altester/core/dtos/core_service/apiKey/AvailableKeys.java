package com.altester.core.dtos.core_service.apiKey;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.enums.AiServiceName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableKeys {
    private Long id;
    private String name;
    private String keyMasked;
    private AiServiceName aiServiceName;

    public static AvailableKeys fromApiKey(ApiKey apiKey) {
        String maskedKey = apiKey.getKeyPrefix() + "*".repeat(8) + apiKey.getKeySuffix();

        return AvailableKeys.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyMasked(maskedKey)
                .aiServiceName(apiKey.getAiServiceName())
                .build();
    }
}
