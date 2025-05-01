package com.altester.ai_grading_service.AiModels;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

public abstract class BaseModel extends AbstractChatModel {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected BaseModel(String apiKey, String modelName, double temperature, Duration timeout) {
        super(apiKey, modelName, temperature, timeout);
    }
}