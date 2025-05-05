package com.altester.core.config;

import com.altester.core.model.ApiKey.enums.AiServiceName;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@Getter
public class AiModelConfiguration {

    private final Map<AiServiceName, List<String>> availableModels = new HashMap<>();

    public AiModelConfiguration(
            @Value("${ai.models.openai}") String openaiModels,
            @Value("${ai.models.anthropic}") String anthropicModels,
            @Value("${ai.models.deepseek}") String deepseekModels,
            @Value("${ai.models.gemini}") String geminiModels
    ) {
        availableModels.put(AiServiceName.OPENAI, parseModelList(openaiModels));
        availableModels.put(AiServiceName.ANTHROPIC_CLAUDE, parseModelList(anthropicModels));
        availableModels.put(AiServiceName.DEEPSEEK, parseModelList(deepseekModels));
        availableModels.put(AiServiceName.GEMINI, parseModelList(geminiModels));
    }

    private List<String> parseModelList(String models) {
        if (models == null || models.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(models.split(","));
    }

    public List<String> getAvailableModels(AiServiceName serviceName) {
        return availableModels.getOrDefault(serviceName, Collections.emptyList());
    }
}
