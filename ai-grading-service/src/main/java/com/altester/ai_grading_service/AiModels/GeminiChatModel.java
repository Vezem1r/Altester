package com.altester.ai_grading_service.AiModels;

import com.altester.ai_grading_service.AiModels.dto.ModelResponses.GeminiResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class GeminiChatModel extends BaseModel {

  public GeminiChatModel(String apiKey, String modelName, double temperature, Duration timeout) {
    super(apiKey, modelName, temperature, timeout);
  }

  @Override
  protected HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Override
  protected Map<String, Object> createRequestBody(
      String userMessage, double tempValue, int maxTokens) {
    Map<String, Object> requestBody = new HashMap<>();

    List<Map<String, Object>> contents = new ArrayList<>();
    Map<String, Object> content = new HashMap<>();
    content.put("role", "user");

    Map<String, String> partObject = new HashMap<>();
    partObject.put("text", userMessage);

    List<Map<String, String>> parts = new ArrayList<>();
    parts.add(partObject);

    content.put("parts", parts);
    contents.add(content);

    requestBody.put("contents", contents);
    requestBody.put(
        "generationConfig",
        Map.of(
            "temperature", tempValue,
            "maxOutputTokens", maxTokens));

    return requestBody;
  }

  @Override
  protected String getApiEndpoint() {
    return "https://generativelanguage.googleapis.com/v1beta/models/"
        + getModelName()
        + ":generateContent?key="
        + getApiKey();
  }

  @Override
  protected String extractResponseText(Map<String, Object> responseBody) {
    try {
      GeminiResponse response = objectMapper.convertValue(responseBody, GeminiResponse.class);

      if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
        return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
      }
    } catch (Exception e) {
      return "Error extracting response: " + e.getMessage();
    }
    return null;
  }

  @Override
  protected String getModelProvider() {
    return "Google Gemini";
  }
}
