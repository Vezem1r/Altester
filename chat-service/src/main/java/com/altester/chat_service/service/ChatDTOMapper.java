package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.model.ChatMessage;
import com.altester.chat_service.model.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatDTOMapper {

    public ChatMessageDTO mapToChatMessageDTO(ChatMessage message, String receiverId) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .receiverId(receiverId)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .build();
    }

    public ConversationDTO mapToConversationDTO(Conversation conversation, String userId) {
        String otherParticipantId = conversation.getOtherParticipantId(userId);

        List<ChatMessage> recentMessages = conversation.getMessages().stream()
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .limit(20)
                .toList();

        List<ChatMessageDTO> messageDTOs = recentMessages.stream()
                .map(message -> mapToChatMessageDTO(message, otherParticipantId))
                .collect(Collectors.toList());

        long unreadCount = recentMessages.stream()
                .filter(message -> !message.isRead() && !message.getSenderId().equals(userId))
                .count();

        String lastMessageContent = recentMessages.isEmpty() ? "" :
                recentMessages.getFirst().getContent();

        return ConversationDTO.builder()
                .id(conversation.getId())
                .participant1Id(conversation.getParticipant1Id())
                .participant2Id(conversation.getParticipant2Id())
                .lastMessageTime(conversation.getLastMessageTime())
                .messages(messageDTOs)
                .unreadCount((int) unreadCount)
                .lastMessageContent(lastMessageContent)
                .build();
    }
}