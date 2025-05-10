package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.model.ChatMessage;
import com.altester.chat_service.model.Conversation;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatDTOMapper {

  public ConversationDTO mapToConversationDTO(Conversation conversation, String currentUserId) {
    String otherParticipantId = conversation.getOtherParticipantId(currentUserId);

    List<ChatMessageDTO> messagesList =
        conversation.getMessages().stream()
            .map(message -> mapToChatMessageDTO(message, otherParticipantId))
            .collect(Collectors.toList());

    long unreadCount =
        conversation.getMessages().stream()
            .filter(message -> !message.isRead() && !message.getSenderId().equals(currentUserId))
            .count();

    String lastMessageContent =
        conversation.getMessages().isEmpty()
            ? null
            : conversation.getMessages().getLast().getContent();

    return ConversationDTO.builder()
        .id(conversation.getId())
        .participant1Id(conversation.getParticipant1Id())
        .participant2Id(conversation.getParticipant2Id())
        .participantName(otherParticipantId)
        .lastMessageTime(conversation.getLastMessageTime())
        .lastMessageContent(lastMessageContent)
        .unreadCount((int) unreadCount)
        .messages(messagesList)
        .online(false)
        .build();
  }

  public ChatMessageDTO mapToChatMessageDTO(ChatMessage message, String otherParticipantId) {
    return ChatMessageDTO.builder()
        .id(message.getId())
        .conversationId(message.getConversation().getId())
        .senderId(message.getSenderId())
        .receiverId(otherParticipantId)
        .content(message.getContent())
        .timestamp(message.getTimestamp())
        .read(message.isRead())
        .build();
  }
}
