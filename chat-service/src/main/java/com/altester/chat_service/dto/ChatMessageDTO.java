package com.altester.chat_service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
  private Long id;
  private Long conversationId;
  private String senderId;
  private String receiverId;
  private String content;
  private LocalDateTime timestamp;
  private boolean read;
}
