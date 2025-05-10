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
  private String participantName;
  private LocalDateTime lastMessageTime;
  private String lastMessageContent;
  private Integer unreadCount;
  private List<ChatMessageDTO> messages;
  private boolean online;
}
