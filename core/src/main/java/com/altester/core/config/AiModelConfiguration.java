package com.altester.core.config;

import com.altester.core.model.ApiKey.enums.AiServiceName;
import java.util.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AiModelConfiguration {

  @Value("${prompt.teacher.max-count}")
  private int maxPromptsPerTeacher;

  @Value("${ai.models.openai}")
  private String openaiModels;

  @Value("${ai.models.anthropic}")
  private String anthropicModels;

  @Value("${ai.models.deepseek}")
  private String deepseekModels;

  @Value("${ai.models.gemini}")
  private String geminiModel;

  private List<String> parseModelList(String models) {
    if (models == null || models.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(models.split(","));
  }

  public List<String> getAvailableModels(AiServiceName serviceName) {
    return switch (serviceName) {
      case OPENAI -> parseModelList(openaiModels);
      case ANTHROPIC_CLAUDE -> parseModelList(anthropicModels);
      case DEEPSEEK -> parseModelList(deepseekModels);
      case GEMINI -> parseModelList(geminiModel);
    };
  }
}
