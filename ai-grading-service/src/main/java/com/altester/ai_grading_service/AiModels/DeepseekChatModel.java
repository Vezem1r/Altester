package com.altester.ai_grading_service.AiModels;

import com.altester.ai_grading_service.AiModels.dto.ModelResponses.DeepseekResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepseekChatModel extends BaseModel {

    public DeepseekChatModel(String apiKey, String modelName, double temperature, Duration timeout) {
        super(apiKey, modelName, temperature, timeout);
    }

    @Override
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    protected Map<String, Object> createRequestBody(String userMessage, double tempValue, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userMessage);
        messages.add(userMessageMap);

        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("temperature", tempValue);
        requestBody.put("max_tokens", maxTokens);

        return requestBody;
    }

    @Override
    protected String getApiEndpoint() {
        return "https://api.deepseek.com/v1/chat/completions";
    }

    @Override
    protected String extractResponseText(Map<String, Object> responseBody) {
        try {
            DeepseekResponse response = objectMapper.convertValue(responseBody, DeepseekResponse.class);

            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().getFirst().getMessage().getContent();
            }
        } catch (Exception e) {
            return "Error extracting response: " + e.getMessage();
        }
        return null;
    }

    @Override
    protected String getModelProvider() {
        return "Deepseek";
    }
}