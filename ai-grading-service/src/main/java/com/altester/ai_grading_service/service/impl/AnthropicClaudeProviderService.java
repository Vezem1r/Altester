package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.ClaudeChatModel;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class AnthropicClaudeProviderService extends AbstractAiProviderService {

    @Value("${ai.anthropic.model}")
    private String model;

    @Value("${ai.anthropic.timeout}")
    private int timeout;

    public AnthropicClaudeProviderService(PromptBuilder promptBuilder) {
        super(promptBuilder);
    }

    @Override
    protected String sendPromptToAi(String prompt, String apiKey, int maxScore) {
        try {
            ClaudeChatModel chatModel = new ClaudeChatModel(
                    apiKey,
                    model,
                    0.1,
                    Duration.ofSeconds(timeout)
            );

            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("Failed to process request with Anthropic Claude: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to process request with Anthropic Claude", e);
        }
    }

    @Override
    protected String getProviderName() {
        return "Anthropic Claude";
    }

    @Override
    public boolean supports(String providerName) {
        return "ANTHROPIC_CLAUDE".equalsIgnoreCase(providerName);
    }
}