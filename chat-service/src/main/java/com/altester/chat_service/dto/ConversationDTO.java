package com.altester.chat_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
  private Long id;
  private String participant1Id;
  private String participant2Id;
  private LocalDateTime lastMessageTime;
  private List<ChatMessageDTO> messages;
  private int unreadCount;
  private String lastMessageContent;
}
