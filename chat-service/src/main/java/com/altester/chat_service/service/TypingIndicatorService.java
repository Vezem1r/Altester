package com.altester.chat_service.service;

public interface TypingIndicatorService {

  /**
   * Sets the typing status of a user in a conversation. When a user is typing, this information
   * will be pushed to the other participant through WebSocket. The typing status automatically
   * expires after a set timeout if not refreshed.
   *
   * @param senderId ID of the user who is typing
   * @param receiverId ID of the user who should receive the typing notification
   * @param conversationId ID of the conversation
   * @param isTyping True if the user is typing, false otherwise
   */
  void setTypingStatus(String senderId, String receiverId, Long conversationId, boolean isTyping);
}
