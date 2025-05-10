package com.altester.chat_service.service.impl;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.service.WebSocketService;
import com.altester.chat_service.util.WebSocketUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  private static final String M_DESTINATION = "/queue/messages";
  private static final String S_DESTINATION = "/topic/status";
  private static final String T_DESTINATION = "/queue/typing";

  @Override
  public void sendChatMessage(String username, ChatMessageDTO message) {
    log.info("Sending chat message to user: {}", username);
    log.debug(
        "Message details: id={}, content={}, sender={}",
        message.getId(),
        message.getContent(),
        message.getSenderId());

    try {
      Map<String, Object> wrapper = WebSocketUtils.createNewMessageResponse(message);

      messagingTemplate.convertAndSendToUser(username, M_DESTINATION, wrapper);
      log.info("Chat message sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending chat message to {}: {}", username, e.getMessage(), e);
    }
  }

  @Override
  public void sendUnreadCount(String username, Long conversationId, long count) {
    log.info(
        "Sending unread count for conversation {}: {} to user: {}",
        conversationId,
        count,
        username);

    try {
      Map<String, Object> response =
          WebSocketUtils.createUnreadCountResponse(conversationId, count);

      messagingTemplate.convertAndSendToUser(username, M_DESTINATION, response);
      log.info("Unread count update sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending unread count update to {}: {}", username, e.getMessage(), e);
    }
  }

  @Override
  public void sendUnreadCountWithBreakdown(
      String username, long totalCount, Map<Long, Long> conversationCounts) {
    log.info("Sending unread count breakdown with total {} to user: {}", totalCount, username);

    try {
      Map<String, Object> response =
          WebSocketUtils.createUnreadCountBreakdownResponse(totalCount, conversationCounts);
      messagingTemplate.convertAndSendToUser(username, M_DESTINATION, response);
      log.info("Unread count breakdown sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending unread count breakdown to {}: {}", username, e.getMessage(), e);
    }
  }

  @Override
  public void sendTypingIndicator(
      String username, String senderUsername, Long conversationId, boolean isTyping) {
    log.info("Sending typing indicator to user: {}", username);

    try {
      Map<String, Object> response =
          WebSocketUtils.createTypingIndicatorResponse(senderUsername, conversationId, isTyping);

      messagingTemplate.convertAndSendToUser(username, T_DESTINATION, response);
      log.debug("Typing indicator sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending typing indicator to {}: {}", username, e.getMessage(), e);
    }
  }

  @Override
  public void sendMessageReadUpdate(String username, Long messageId, boolean isRead) {
    log.info(
        "Sending read status update for message {}: {} to user: {}", messageId, isRead, username);

    try {
      Map<String, Object> response =
          WebSocketUtils.createMessageReadStatusResponse(messageId, isRead);

      messagingTemplate.convertAndSendToUser(username, M_DESTINATION, response);
      log.debug("Read status update sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending read status update to {}: {}", username, e.getMessage(), e);
    }
  }

  @Override
  public void broadcastUserStatus(String username, boolean isOnline) {
    log.info("Broadcasting user status: {} is {}", username, isOnline ? "online" : "offline");

    try {
      Map<String, Object> statusUpdate =
          WebSocketUtils.createUserStatusChangeResponse(username, isOnline);

      messagingTemplate.convertAndSend(S_DESTINATION, statusUpdate);
      log.info("User status broadcast successfully");
    } catch (Exception e) {
      log.error("Error broadcasting user status: {}", e.getMessage(), e);
    }
  }
}
