package com.altester.ai_grading_service.AiModels;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.time.Duration;
import java.util.Collections;

public abstract class BaseModel extends AbstractChatModel {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected BaseModel(String apiKey, String modelName, double temperature, Duration timeout) {
        super(apiKey, modelName, temperature, timeout);
    }

    /**
     * Generate a response to a prompt using the AI model.
     * This method creates a ChatRequest with the given prompt
     * and calls doChat to get a response.
     *
     * @param prompt The prompt to send to the model
     * @return The model's response as a string
     */
    public String generate(String prompt) {
        UserMessage userMessage = UserMessage.from(prompt);

        ChatRequest request = ChatRequest.builder()
                .messages(Collections.singletonList(userMessage))
                .build();

        ChatResponse response = this.doChat(request);

        if (response != null && response.aiMessage() != null) {
            return response.aiMessage().text();
        }

        return "Error: No response generated";
    }
}