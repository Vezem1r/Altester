package com.altester.chat_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

  @NotBlank(message = "Receiver ID cannot be empty")
  private String receiverId;

  @NotBlank(message = "Message content cannot be empty")
  private String content;

  private Long conversationId;
}
