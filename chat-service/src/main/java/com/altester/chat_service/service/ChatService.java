package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ChatService {

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
   * Gets the ID of the first unread message in a conversation for the specified user.
   *
   * @param userId ID of the user
   * @param conversationId ID of the conversation
   * @return ID of the first unread message, or null if none exists
   */
  Long getFirstUnreadMessageId(String userId, Long conversationId);
}
