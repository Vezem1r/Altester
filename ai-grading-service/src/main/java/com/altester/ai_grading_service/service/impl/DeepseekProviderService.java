package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.DeepseekChatModel;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class DeepseekProviderService extends AbstractAiProviderService {

    @Value("${ai.deepseek.model}")
    private String model;

    @Value("${ai.deepseek.timeout}")
    private int timeout;

    public DeepseekProviderService(PromptBuilder promptBuilder) {
        super(promptBuilder);
    }

    @Override
    protected String sendPromptToAi(String prompt, String apiKey, int maxScore) {
        try {
            DeepseekChatModel chatModel = new DeepseekChatModel(
                    apiKey,
                    model,
                    0.1,
                    Duration.ofSeconds(timeout)
            );

            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("Failed to process request with Deepseek: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to process request with Deepseek", e);
        }
    }

    @Override
    protected String getProviderName() {
        return "Deepseek";
    }

    @Override
    public boolean supports(String providerName) {
        return "DEEPSEEK".equalsIgnoreCase(providerName);
    }
}