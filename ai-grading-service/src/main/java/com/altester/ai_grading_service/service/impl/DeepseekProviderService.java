package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.DeepseekChatModel;
import com.altester.ai_grading_service.exception.AiApiServiceException;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeepseekProviderService extends AbstractAiProviderService {

  @Value("${ai.timeout}")
  private int timeout;

  @Value("${ai.temperature}")
  private double temperature;

  public DeepseekProviderService(PromptBuilder promptBuilder) {
    super(promptBuilder);
  }

  @Override
  protected String sendPromptToAi(String prompt, String apiKey, String model, int maxScore)
      throws AiApiServiceException {
    try {
      DeepseekChatModel chatModel =
          new DeepseekChatModel(apiKey, model, temperature, Duration.ofSeconds(timeout));

      return chatModel.generate(prompt);
    } catch (Exception e) {
      log.error("Failed to process request with Deepseek: {}", e.getMessage(), e);
      if (e instanceof AiApiServiceException) {
        throw e;
      }
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
