package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.dto.MessageRequest;
import com.altester.chat_service.model.Conversation;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public interface ChatService {

  /**
   * Retrieves an existing conversation between two users or creates a new one if it doesn't exist.
   *
   * @param participant1Id ID of the first participant
   * @param participant2Id ID of the second participant
   * @return The existing or newly created Conversation
   */
  Conversation getOrCreateConversation(String participant1Id, String participant2Id);

  /**
   * Gets the count of unread messages for each conversation involving the specified user.
   *
   * @param userId ID of the user
   * @return Map of conversation IDs to their unread message counts
   */
  Map<Long, Long> getUnreadCountsByConversation(String userId);

  /**
   * Sends a message from one user to another.
   *
   * @param senderId ID of the message sender
   * @param request The message request containing content and recipient information
   * @return DTO representing the sent message
   */
  ChatMessageDTO sendMessage(String senderId, MessageRequest request);

  /**
   * Retrieves all conversations for the specified user.
   *
   * @param userId ID of the user
   * @return List of conversation DTOs
   */
  List<ConversationDTO> getConversationsForUser(String userId);

  /**
   * Retrieves paginated conversations for the specified user.
   *
   * @param userId ID of the user
   * @param page Page number (0-based)
   * @param size Size of each page
   * @return Page of conversation DTOs
   */
  Page<ConversationDTO> getPaginatedConversations(String userId, int page, int size);

  /**
   * Retrieves paginated messages for a specific conversation.
   *
   * @param userId ID of the requesting user
   * @param conversationId ID of the conversation
   * @param page Page number (0-based)
   * @param size Size of each page
   * @return Page of message DTOs
   */
  Page<ChatMessageDTO> getConversationMessages(
      String userId, Long conversationId, int page, int size);

  /**
   * Retrieves all unread messages for the specified user across all conversations.
   *
   * @param userId ID of the user
   * @return List of unread message DTOs
   */
  List<ChatMessageDTO> getUnreadMessages(String userId);

  /**
   * Marks all messages in a conversation as read for the specified user.
   *
   * @param userId ID of the user
   * @param conversationId ID of the conversation
   * @return Number of messages marked as read
   */
  int markMessagesAsRead(String userId, Long conversationId);

  /**
   * Gets the ID of the first unread message in a conversation for the specified user.
   *
   * @param userId ID of the user
   * @param conversationId ID of the conversation
   * @return ID of the first unread message, or null if none exists
   */
  Long getFirstUnreadMessageId(String userId, Long conversationId);
}
