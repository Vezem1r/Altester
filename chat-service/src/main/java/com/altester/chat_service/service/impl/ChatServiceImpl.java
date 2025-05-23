package com.altester.chat_service.service.impl;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.exception.ChatException;
import com.altester.chat_service.model.ChatMessage;
import com.altester.chat_service.model.Conversation;
import com.altester.chat_service.repository.ChatMessageRepository;
import com.altester.chat_service.repository.ConversationRepository;
import com.altester.chat_service.service.ChatDTOMapper;
import com.altester.chat_service.service.ChatService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

  private static final String ERROR_MESSAGE = "Conversation not found";

  private final ChatMessageRepository chatMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ChatDTOMapper chatDTOMapper;

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDTO> getConversationsForUser(String userId) {
    List<Conversation> conversations = conversationRepository.findConversationsForUser(userId);

    return conversations.stream()
        .map(
            conversation -> {
              ConversationDTO dto = chatDTOMapper.mapToConversationDTO(conversation, userId);
              dto.setOnline(false);
              return dto;
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ConversationDTO> getPaginatedConversations(String userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageTime"));
    Page<Conversation> conversationsPage =
        conversationRepository.findConversationsForUserPaginated(userId, pageable);

    return conversationsPage.map(
        conversation -> {
          ConversationDTO dto = chatDTOMapper.mapToConversationDTO(conversation, userId);
          dto.setOnline(false);
          return dto;
        });
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatMessageDTO> getConversationMessages(
      String userId, Long conversationId, int page, int size) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ChatException(ERROR_MESSAGE));

    if (!conversation.hasParticipant(userId)) {
      throw new ChatException("You are not a participant in this conversation");
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
    Page<ChatMessage> messagesPage =
        chatMessageRepository.findByConversationIdOrderByTimestampDesc(conversationId, pageable);

    String otherParticipantId = conversation.getOtherParticipantId(userId);
    return messagesPage.map(
        message -> chatDTOMapper.mapToChatMessageDTO(message, otherParticipantId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageDTO> getUnreadMessages(String userId) {
    List<Conversation> conversations = conversationRepository.findConversationsForUser(userId);

    return conversations.stream()
        .flatMap(
            conversation -> {
              if (conversation.getMessages() == null) {
                return Stream.empty();
              }
              return conversation.getMessages().stream();
            })
        .filter(message -> !message.isRead() && !message.getSenderId().equals(userId))
        .map(
            message -> {
              String otherParticipantId = message.getSenderId();
              return chatDTOMapper.mapToChatMessageDTO(message, otherParticipantId);
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Long getFirstUnreadMessageId(String userId, Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ChatException(ERROR_MESSAGE));

    if (!conversation.hasParticipant(userId)) {
      throw new ChatException("You are not a participant in this conversation");
    }

    String otherParticipantId = conversation.getOtherParticipantId(userId);

    List<ChatMessage> messages =
        chatMessageRepository.findFirstUnreadMessages(conversationId, otherParticipantId);
    return messages.isEmpty() ? null : messages.getFirst().getId();
  }
}
