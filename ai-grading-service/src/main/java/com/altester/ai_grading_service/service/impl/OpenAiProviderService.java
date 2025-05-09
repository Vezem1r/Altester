package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.OpenAiChatModel;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OpenAiProviderService extends AbstractAiProviderService {

  @Value("${ai.timeout}")
  private int timeout;

  @Value("${ai.temperature}")
  private double temperature;

  public OpenAiProviderService(PromptBuilder promptBuilder) {
    super(promptBuilder);
  }

  @Override
  protected String sendPromptToAi(String prompt, String apiKey, String model, int maxScore) {
    try {
      OpenAiChatModel chatModel =
          new OpenAiChatModel(apiKey, model, temperature, Duration.ofSeconds(timeout));

      return chatModel.generate(prompt);
    } catch (Exception e) {
      log.error("Failed to process request with OpenAI: {}", e.getMessage(), e);
      throw new AiServiceException("Failed to process request with OpenAI", e);
    }
  }

  @Override
  protected String getProviderName() {
    return "OpenAI";
  }

  @Override
  public boolean supports(String providerName) {
    return "OPENAI".equalsIgnoreCase(providerName);
  }
}
