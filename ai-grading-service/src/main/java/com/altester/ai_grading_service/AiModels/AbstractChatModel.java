package com.altester.ai_grading_service.AiModels;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Abstract base class for all AI chat models
 */
public abstract class AbstractChatModel implements ChatLanguageModel {

    protected final String apiKey;
    protected final String modelName;
    protected final double temperature;
    protected final Duration timeout;
    protected final RestTemplate restTemplate;

    protected AbstractChatModel(String apiKey, String modelName, double temperature, Duration timeout) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.temperature = temperature;
        this.timeout = timeout;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        HttpHeaders headers = createHeaders();

        String userMessage = extractUserMessage(chatRequest);
        Map<String, Object> requestBody = createRequestBody(
                userMessage,
                chatRequest.parameters().temperature() != null ? chatRequest.parameters().temperature() : temperature,
                chatRequest.parameters().maxOutputTokens() != null ? chatRequest.parameters().maxOutputTokens() : 4000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    getApiEndpoint(),
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                String extractedText = extractResponseText(responseBody);
                if (extractedText != null) {
                    return ChatResponse.builder()
                            .aiMessage(AiMessage.from(extractedText))
                            .build();
                }
            }
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("Error: Unexpected response format from " + getModelProvider() + " API"))
                    .build();
        } catch (Exception e) {
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("Error: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Extract the user message from the chat request
     */
    protected String extractUserMessage(ChatRequest chatRequest) {
        ChatMessage message = chatRequest.messages().getLast();
        if (message instanceof UserMessage) {
            return ((UserMessage) message).singleText();
        } else if (message instanceof AiMessage) {
            return ((AiMessage) message).text();
        }
        return message.toString();
    }

    /**
     * Create headers for the API request
     */
    protected abstract HttpHeaders createHeaders();

    /**
     * Create request body for the API call
     */
    protected abstract Map<String, Object> createRequestBody(String userMessage, double tempValue, int maxTokens);

    /**
     * Get the API endpoint URL
     */
    protected abstract String getApiEndpoint();

    /**
     * Extract text response from API response body
     */
    protected abstract String extractResponseText(Map<String, Object> responseBody);

    /**
     * Get the model provider name for error messages
     */
    protected abstract String getModelProvider();
}