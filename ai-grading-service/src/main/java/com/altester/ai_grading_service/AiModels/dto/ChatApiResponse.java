package com.altester.ai_grading_service.AiModels.dto;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ChatApiResponse extends ChatResponse {
  private final HttpStatusCode statusCode;
  private final AiMessage message;

  ChatApiResponse(HttpStatusCode statusCode, AiMessage message) {
    super(ChatResponse.builder().aiMessage(message));
    this.statusCode = statusCode;
    this.message = message;
  }

  public static ChatResponse of(HttpStatusCode statusCode, AiMessage message) {
    return new ChatApiResponse(statusCode, message);
  }
}
