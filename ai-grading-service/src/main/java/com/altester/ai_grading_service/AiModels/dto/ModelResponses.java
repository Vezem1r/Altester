package com.altester.ai_grading_service.AiModels.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class ModelResponses {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenAiResponse {
        @JsonProperty("choices")
        private List<OpenAiChoice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenAiChoice {
        @JsonProperty("message")
        private OpenAiMessage message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenAiMessage {
        @JsonProperty("content")
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClaudeResponse {
        @JsonProperty("content")
        private List<ClaudeContent> content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClaudeContent {
        @JsonProperty("text")
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        @JsonProperty("candidates")
        private List<GeminiCandidate> candidates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiCandidate {
        @JsonProperty("content")
        private GeminiContent content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiContent {
        @JsonProperty("parts")
        private List<GeminiPart> parts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiPart {
        @JsonProperty("text")
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepseekResponse {
        @JsonProperty("choices")
        private List<DeepseekChoice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepseekChoice {
        @JsonProperty("message")
        private DeepseekMessage message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepseekMessage {
        @JsonProperty("content")
        private String content;
    }
}