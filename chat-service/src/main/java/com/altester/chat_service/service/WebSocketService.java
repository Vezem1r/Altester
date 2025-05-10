package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import java.util.Map;

public interface WebSocketService {

  /**
   * Sends a chat message to a specific user through WebSocket.
   *
   * @param username The username of the recipient
   * @param message The message to send
   */
  void sendChatMessage(String username, ChatMessageDTO message);

  /**
   * Sends an unread count update for a specific conversation to a user.
   *
   * @param username The username of the recipient
   * @param conversationId The ID of the conversation
   * @param count The number of unread messages
   */
  void sendUnreadCount(String username, Long conversationId, long count);

  /**
   * Sends a breakdown of unread messages across all conversations to a user.
   *
   * @param username The username of the recipient
   * @param totalCount The total number of unread messages
   * @param conversationCounts Map of conversation IDs to their respective unread counts
   */
  void sendUnreadCountWithBreakdown(
      String username, long totalCount, Map<Long, Long> conversationCounts);

  /**
   * Sends a typing indicator notification to a user.
   *
   * @param username The username of the recipient
   * @param senderUsername The username of the user who is typing
   * @param conversationId The ID of the conversation
   * @param isTyping True if the user is typing, false if they stopped typing
   */
  void sendTypingIndicator(
      String username, String senderUsername, Long conversationId, boolean isTyping);

  /**
   * Sends a read status update for a message to a user.
   *
   * @param username The username of the recipient
   * @param messageId The ID of the message
   * @param isRead True if the message has been read, false otherwise
   */
  void sendMessageReadUpdate(String username, Long messageId, boolean isRead);

  /**
   * Broadcasts a user's online/offline status to all connected users.
   *
   * @param username The username of the user whose status changed
   * @param isOnline True if the user is now online, false if offline
   */
  void broadcastUserStatus(String username, boolean isOnline);
}
