package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.GeminiChatModel;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class GeminiProviderService extends AbstractAiProviderService {

    @Value("${ai.gemini.model}")
    private String model;

    @Value("${ai.gemini.timeout}")
    private int timeout;

    public GeminiProviderService(PromptBuilder promptBuilder) {
        super(promptBuilder);
    }

    @Override
    protected String sendPromptToAi(String prompt, String apiKey, int maxScore) {
        try {
            GeminiChatModel chatModel = new GeminiChatModel(
                    apiKey,
                    model,
                    0.1,
                    Duration.ofSeconds(timeout)
            );

            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("Failed to process request with Gemini: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to process request with Gemini", e);
        }
    }

    @Override
    protected String getProviderName() {
        return "Google Gemini";
    }

    @Override
    public boolean supports(String providerName) {
        return "GEMINI".equalsIgnoreCase(providerName);
    }
}