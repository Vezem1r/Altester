package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  public void sendChatMessage(String username, ChatMessageDTO message) {
    log.info("Sending chat message to user: {}", username);
    log.debug(
        "Message details: id={}, content={}, sender={}",
        message.getId(),
        message.getContent(),
        message.getSenderId());

    try {
      Map<String, Object> wrapper = Map.of("type", "NEW_MESSAGE", "message", message);

      messagingTemplate.convertAndSendToUser(username, "/queue/messages", wrapper);
      log.info("Chat message sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending chat message to {}: {}", username, e.getMessage(), e);
    }
  }

  public void sendUnreadCount(String username, Long conversationId, long count) {
    log.info(
        "Sending unread count for conversation {}: {} to user: {}",
        conversationId,
        count,
        username);

    try {
      Map<String, Object> response = new HashMap<>();
      response.put("type", "UNREAD_COUNT");
      response.put("conversationId", conversationId);
      response.put("unreadCount", count);

      messagingTemplate.convertAndSendToUser(username, "/queue/messages", response);
      log.info("Unread count update sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending unread count update to {}: {}", username, e.getMessage(), e);
    }
  }

  public void sendTypingIndicator(
      String username, String senderUsername, Long conversationId, boolean isTyping) {
    log.info("Sending typing indicator to user: {}", username);

    try {
      Map<String, Object> response =
          Map.of(
              "type", "TYPING_INDICATOR",
              "senderUsername", senderUsername,
              "conversationId", conversationId,
              "isTyping", isTyping);

      messagingTemplate.convertAndSendToUser(username, "/queue/typing", response);
      log.debug("Typing indicator sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending typing indicator to {}: {}", username, e.getMessage(), e);
    }
  }

  public void sendMessageReadUpdate(String username, Long messageId, boolean isRead) {
    log.info(
        "Sending read status update for message {}: {} to user: {}", messageId, isRead, username);

    try {
      Map<String, Object> response =
          Map.of(
              "type", "MESSAGE_READ_STATUS",
              "messageId", messageId,
              "isRead", isRead);

      messagingTemplate.convertAndSendToUser(username, "/queue/messages", response);
      log.debug("Read status update sent successfully to {}", username);
    } catch (Exception e) {
      log.error("Error sending read status update to {}: {}", username, e.getMessage(), e);
    }
  }
}
