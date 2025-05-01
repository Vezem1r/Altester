package com.altester.ai_grading_service.AiModels;

import com.altester.ai_grading_service.AiModels.dto.ModelResponses.ClaudeResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaudeChatModel extends BaseModel {

    public ClaudeChatModel(String apiKey, String modelName, double temperature, Duration timeout) {
        super(apiKey, modelName, temperature, timeout);
    }

    @Override
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("anthropic-version", "2023-06-01");
        return headers;
    }

    @Override
    protected Map<String, Object> createRequestBody(String userMessage, double tempValue, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);
        messages.add(message);

        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("temperature", tempValue);
        requestBody.put("max_tokens", maxTokens);

        return requestBody;
    }

    @Override
    protected String getApiEndpoint() {
        return "https://api.anthropic.com/v1/messages";
    }

    @Override
    protected String extractResponseText(Map<String, Object> responseBody) {
        try {
            ClaudeResponse response = objectMapper.convertValue(responseBody, ClaudeResponse.class);

            if (response.getContent() != null && !response.getContent().isEmpty()) {
                return response.getContent().getFirst().getText();
            }
        } catch (Exception e) {
            return "Error extracting response: " + e.getMessage();
        }
        return null;
    }

    @Override
    protected String getModelProvider() {
        return "Anthropic Claude";
    }
}