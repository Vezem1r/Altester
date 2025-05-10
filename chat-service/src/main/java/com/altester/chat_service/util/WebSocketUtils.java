package com.altester.chat_service.util;

import com.altester.chat_service.dto.ChatMessageDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebSocketUtils {

  private WebSocketUtils() {}

  private static final String FLAG = "conversationId";

  /**
   * Creates a basic response with just the message type
   *
   * @param type The message type
   * @return A map containing the message type
   */
  public static Map<String, Object> createResponse(String type) {
    Map<String, Object> response = new HashMap<>();
    response.put("type", type);
    return response;
  }

  /**
   * Creates a response for initial data when a user connects
   *
   * @param unreadMessages List of unread messages
   * @param conversations List of conversations for the user
   * @param availableUsers Map of available users
   * @param onlineUsers Set of online users
   * @return A map containing the initial data response
   */
  public static Map<String, Object> createInitialDataResponse(
      List<ChatMessageDTO> unreadMessages,
      Object conversations,
      Map<String, Object> availableUsers,
      Set<String> onlineUsers) {
    Map<String, Object> response = createResponse("INITIAL_DATA");
    response.put("unreadMessages", unreadMessages);
    response.put("conversations", conversations);
    response.put("availableUsers", availableUsers);
    response.put("onlineUsers", onlineUsers);
    return response;
  }

  /**
   * Creates a response for a new chat message
   *
   * @param message The new chat message
   * @return A map containing the new message response
   */
  public static Map<String, Object> createNewMessageResponse(ChatMessageDTO message) {
    Map<String, Object> response = createResponse("NEW_MESSAGE");
    response.put("message", message);
    return response;
  }

  /**
   * Creates a response for marking messages as read
   *
   * @param conversationId The conversation ID
   * @param markedCount The number of messages marked as read
   * @return A map containing the marked read response
   */
  public static Map<String, Object> createMessagesMarkedReadResponse(
      Long conversationId, int markedCount) {
    Map<String, Object> response = createResponse("MESSAGES_MARKED_READ");
    response.put(FLAG, conversationId);
    response.put("markedCount", markedCount);
    return response;
  }

  /**
   * Creates a response for a message sent confirmation
   *
   * @param message The sent message
   * @return A map containing the message sent response
   */
  public static Map<String, Object> createMessageSentResponse(ChatMessageDTO message) {
    Map<String, Object> response = createResponse("MESSAGE_SENT");
    response.put("message", message);
    return response;
  }

  /**
   * Creates a response for an unread count update for a specific conversation
   *
   * @param conversationId The conversation ID
   * @param count The updated unread count
   * @return A map containing the unread count response
   */
  public static Map<String, Object> createUnreadCountResponse(Long conversationId, long count) {
    Map<String, Object> response = createResponse("UNREAD_COUNT");
    response.put(FLAG, conversationId);
    response.put("unreadCount", count);
    return response;
  }

  /**
   * Creates a response for an unread count update with breakdown by conversation
   *
   * @param totalCount The total unread count
   * @param conversationCounts Map of conversation IDs to their unread counts
   * @return A map containing the unread count breakdown response
   */
  public static Map<String, Object> createUnreadCountBreakdownResponse(
      long totalCount, Map<Long, Long> conversationCounts) {
    Map<String, Object> response = createResponse("UNREAD_COUNT");
    response.put(FLAG, null);
    response.put("unreadCount", totalCount);
    response.put("conversationBreakdown", conversationCounts);
    return response;
  }

  /**
   * Creates a response for a typing indicator
   *
   * @param senderUsername The username of the person typing
   * @param conversationId The conversation ID
   * @param isTyping Whether the user is typing or stopped typing
   * @return A map containing the typing indicator response
   */
  public static Map<String, Object> createTypingIndicatorResponse(
      String senderUsername, Long conversationId, boolean isTyping) {
    Map<String, Object> response = createResponse("TYPING_INDICATOR");
    response.put("senderUsername", senderUsername);
    response.put(FLAG, conversationId);
    response.put("isTyping", isTyping);
    return response;
  }

  /**
   * Creates a response for a message read status update
   *
   * @param messageId The message ID
   * @param isRead Whether the message is read
   * @return A map containing the read status response
   */
  public static Map<String, Object> createMessageReadStatusResponse(
      Long messageId, boolean isRead) {
    Map<String, Object> response = createResponse("MESSAGE_READ_STATUS");
    response.put("messageId", messageId);
    response.put("isRead", isRead);
    return response;
  }

  /**
   * Creates a response for a user status change broadcast
   *
   * @param username The username
   * @param isOnline Whether the user is online
   * @return A map containing the user status response
   */
  public static Map<String, Object> createUserStatusChangeResponse(
      String username, boolean isOnline) {
    Map<String, Object> response = createResponse("USER_STATUS_CHANGE");
    response.put("username", username);
    response.put("online", isOnline);
    return response;
  }

  /**
   * Creates a response for a user status query
   *
   * @param username The username
   * @param isOnline Whether the user is online
   * @return A map containing the user status response
   */
  public static Map<String, Object> createUserStatusResponse(String username, boolean isOnline) {
    Map<String, Object> response = createResponse("USER_STATUS");
    response.put("username", username);
    response.put("online", isOnline);
    return response;
  }
}
