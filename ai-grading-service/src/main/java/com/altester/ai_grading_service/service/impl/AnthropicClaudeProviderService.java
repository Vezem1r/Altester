package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.AiModels.ClaudeChatModel;
import com.altester.ai_grading_service.exception.AiApiServiceException;
import com.altester.ai_grading_service.util.PromptBuilder;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class AnthropicClaudeProviderService extends AbstractAiProviderService {

  @Value("${ai.timeout}")
  private int timeout;

  @Value("${ai.temperature}")
  private double temperature;

  public AnthropicClaudeProviderService(PromptBuilder promptBuilder) {
    super(promptBuilder);
  }

  @Override
  protected String sendPromptToAi(String prompt, String apiKey, String model, int maxScore)
      throws AiApiServiceException {
    try {
      ClaudeChatModel chatModel =
          new ClaudeChatModel(apiKey, model, temperature, Duration.ofSeconds(timeout));

      return chatModel.generate(prompt);
    } catch (HttpClientErrorException e) {
      String errorBody = e.getResponseBodyAsString();
      HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

      log.error("Anthropic Claude  API error - Status: {}, Body: {}", status, errorBody);

      throw new AiApiServiceException(
              String.format("Anthropic Claude  API error: %s - %s", status, parseErrorMessage(errorBody)),
              status,
              errorBody
      );
    } catch (AiApiServiceException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to process request with Anthropic Claude: {}", e.getMessage(), e);
      throw new AiApiServiceException("Failed to process Anthropic Claude request", e);
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
