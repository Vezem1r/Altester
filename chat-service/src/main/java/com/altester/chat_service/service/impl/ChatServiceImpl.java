package com.altester.chat_service.service.impl;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.dto.MessageRequest;
import com.altester.chat_service.exception.ChatException;
import com.altester.chat_service.model.ChatMessage;
import com.altester.chat_service.model.Conversation;
import com.altester.chat_service.model.User;
import com.altester.chat_service.repository.ChatMessageRepository;
import com.altester.chat_service.repository.ConversationRepository;
import com.altester.chat_service.repository.GroupRepository;
import com.altester.chat_service.repository.UserRepository;
import com.altester.chat_service.service.ChatDTOMapper;
import com.altester.chat_service.service.ChatService;
import com.altester.chat_service.service.UserStatusService;
import com.altester.chat_service.service.WebSocketService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final WebSocketService webSocketService;
  private final ChatDTOMapper chatDTOMapper;
  private final UserStatusService userStatusService;

  private boolean canUsersSendMessages(String senderId, String receiverId) {
    User sender =
        userRepository
            .findByUsername(senderId)
            .orElseThrow(() -> new ChatException("Sender not found: " + senderId));

    User receiver =
        userRepository
            .findByUsername(receiverId)
            .orElseThrow(() -> new ChatException("Receiver not found: " + receiverId));

    if (sender.isTeacher() && receiver.isStudent()) {
      return groupRepository.isStudentInTeacherGroup(senderId, receiverId);
    }

    if (sender.isStudent() && receiver.isTeacher()) {
      return groupRepository.isStudentInTeacherGroup(receiverId, senderId);
    }
    return true;
  }

  @Override
  @Transactional
  public Conversation getOrCreateConversation(String participant1Id, String participant2Id) {
    if (!canUsersSendMessages(participant1Id, participant2Id)
        || !canUsersSendMessages(participant2Id, participant1Id)) {
      throw new ChatException("Users cannot exchange messages");
    }

    Optional<Conversation> existingConversation =
        conversationRepository.findConversationBetween(participant1Id, participant2Id);

    if (existingConversation.isPresent()) {
      return existingConversation.get();
    }

    Conversation newConversation =
        Conversation.builder()
            .participant1Id(participant1Id)
            .participant2Id(participant2Id)
            .lastMessageTime(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

    return conversationRepository.save(newConversation);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, Long> getUnreadCountsByConversation(String userId) {
    List<Conversation> conversations = conversationRepository.findConversationsForUser(userId);
    Map<Long, Long> conversationUnreadCounts = new HashMap<>();

    for (Conversation conversation : conversations) {
      String otherParticipantId = conversation.getOtherParticipantId(userId);
      long unreadCount =
          chatMessageRepository.countUnreadMessagesByConversation(
              conversation.getId(), otherParticipantId);

      conversationUnreadCounts.put(conversation.getId(), unreadCount);
    }

    return conversationUnreadCounts;
  }

  @Override
  @Transactional
  public ChatMessageDTO sendMessage(String senderId, MessageRequest request) {
    String receiverId = request.getReceiverId();

    if (!canUsersSendMessages(senderId, receiverId)) {
      throw new ChatException("You cannot send messages to this user");
    }

    Conversation conversation;
    if (request.getConversationId() != null) {
      conversation =
          conversationRepository
              .findById(request.getConversationId())
              .orElseThrow(() -> new ChatException(ERROR_MESSAGE));

      if (!conversation.hasParticipant(senderId)) {
        throw new ChatException("You are not a participant in this conversation");
      }

      if (!conversation.hasParticipant(receiverId)) {
        throw new ChatException("Receiver is not a participant in this conversation");
      }
    } else {
      conversation = getOrCreateConversation(senderId, receiverId);
    }

    ChatMessage message =
        ChatMessage.builder()
            .conversation(conversation)
            .senderId(senderId)
            .content(request.getContent())
            .timestamp(LocalDateTime.now())
            .read(false)
            .build();

    ChatMessage savedMessage = chatMessageRepository.save(message);
    conversation.setLastMessageTime(savedMessage.getTimestamp());
    conversationRepository.save(conversation);

    ChatMessageDTO messageDTO = chatDTOMapper.mapToChatMessageDTO(savedMessage, receiverId);

    webSocketService.sendChatMessage(receiverId, messageDTO);

    try {
      long unreadCount =
          chatMessageRepository.countUnreadMessagesByConversation(conversation.getId(), receiverId);
      webSocketService.sendUnreadCount(receiverId, conversation.getId(), unreadCount);

      List<ChatMessageDTO> allUnreadMessages = getUnreadMessages(receiverId);
      Map<Long, Long> conversationUnreadCounts = getUnreadCountsByConversation(receiverId);
      webSocketService.sendUnreadCountWithBreakdown(
          receiverId, allUnreadMessages.size(), conversationUnreadCounts);
    } catch (Exception e) {
      log.error("Error sending unread count updates: {}", e.getMessage(), e);
    }

    return messageDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDTO> getConversationsForUser(String userId) {
    List<Conversation> conversations = conversationRepository.findConversationsForUser(userId);

    return conversations.stream()
        .map(
            conversation -> {
              ConversationDTO dto = chatDTOMapper.mapToConversationDTO(conversation, userId);
              String otherParticipantId = conversation.getOtherParticipantId(userId);
              dto.setOnline(userStatusService.isUserOnline(otherParticipantId));
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
          String otherParticipantId = conversation.getOtherParticipantId(userId);
          dto.setOnline(userStatusService.isUserOnline(otherParticipantId));
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
  @Transactional
  public int markMessagesAsRead(String userId, Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ChatException(ERROR_MESSAGE));

    if (!conversation.hasParticipant(userId)) {
      throw new ChatException("You are not a participant in this conversation");
    }

    String otherParticipantId = conversation.getOtherParticipantId(userId);
    int updatedCount = chatMessageRepository.markMessagesAsRead(conversationId, otherParticipantId);

    if (updatedCount > 0) {
      List<ChatMessage> markedMessages =
          chatMessageRepository.findRecentlyMarkedAsRead(conversationId, otherParticipantId);

      for (ChatMessage message : markedMessages) {
        webSocketService.sendMessageReadUpdate(message.getSenderId(), message.getId(), true);
      }

      long newUnreadCount =
          chatMessageRepository.countUnreadMessagesByConversation(
              conversationId, otherParticipantId);
      webSocketService.sendUnreadCount(userId, conversationId, newUnreadCount);

      List<ChatMessageDTO> allUnreadMessages = getUnreadMessages(userId);
      Map<Long, Long> conversationUnreadCounts = getUnreadCountsByConversation(userId);
      webSocketService.sendUnreadCountWithBreakdown(
          userId, allUnreadMessages.size(), conversationUnreadCounts);
    }

    return updatedCount;
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
