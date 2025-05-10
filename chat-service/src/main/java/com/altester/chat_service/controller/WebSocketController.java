package com.altester.chat_service.controller;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.MessageRequest;
import com.altester.chat_service.exception.AuthenticationException;
import com.altester.chat_service.model.User;
import com.altester.chat_service.repository.GroupRepository;
import com.altester.chat_service.service.ChatService;
import com.altester.chat_service.service.TypingIndicatorService;
import com.altester.chat_service.service.UserStatusService;
import com.altester.chat_service.util.WebSocketUtils;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

  private final ChatService chatService;
  private final TypingIndicatorService typingIndicatorService;
  private final GroupRepository groupRepository;
  private final UserStatusService userStatusService;

  private static final String FLAG = "conversationId";

  @MessageMapping("/messages.connect")
  @SendToUser("/queue/messages")
  public Map<String, Object> handleConnection(SimpMessageHeaderAccessor headerAccessor) {
    Principal user = headerAccessor.getUser();
    if (user == null) {
      log.error("No authenticated user found in message headers");
      throw new AuthenticationException("Authentication required");
    }

    String username = user.getName();
    log.info("WebSocket connection established for user: {}", username);

    userStatusService.setUserOnline(username);

    List<ChatMessageDTO> unreadMessages = chatService.getUnreadMessages(username);

    Map<String, Object> availableUsers = new HashMap<>();
    availableUsers.put("users", getAvailableUsersForChat(username));

    log.info(
        "Sending initial data to user {}: {} unread messages", username, unreadMessages.size());

    return WebSocketUtils.createInitialDataResponse(
        unreadMessages,
        chatService.getConversationsForUser(username),
        availableUsers,
        userStatusService.getOnlineUsers());
  }

  @EventListener
  public void handleSessionDisconnect(SessionDisconnectEvent event) {
    SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
    Principal user = headers.getUser();

    if (user != null) {
      String username = user.getName();
      log.info("WebSocket connection closed for user: {}", username);

      userStatusService.setUserOffline(username);
    }
  }

  private List<Map<String, Object>> getAvailableUsersForChat(String username) {
    List<User> availableUsers;

    List<User> teacherGroups = groupRepository.findTeachersForStudent(username);

    if (!teacherGroups.isEmpty()) {
      availableUsers = teacherGroups;
    } else {
      availableUsers = groupRepository.findStudentsForTeacher(username);
    }

    return availableUsers.stream()
        .map(
            user -> {
              Map<String, Object> userMap = new HashMap<>();
              userMap.put("username", user.getUsername());
              userMap.put("role", user.getRole());
              userMap.put("online", userStatusService.isUserOnline(user.getUsername()));
              return userMap;
            })
        .collect(Collectors.toList());
  }

  @MessageMapping("/messages.send")
  @SendToUser("/queue/messages")
  public Map<String, Object> sendMessage(
      @Payload MessageRequest messageRequest, Principal principal) {
    log.debug(
        "Received message from {} to {}: {}",
        principal.getName(),
        messageRequest.getReceiverId(),
        messageRequest.getContent());

    ChatMessageDTO message = chatService.sendMessage(principal.getName(), messageRequest);
    return WebSocketUtils.createMessageSentResponse(message);
  }

  @MessageMapping("/messages.markRead")
  @SendToUser("/queue/messages")
  public Map<String, Object> markMessagesAsRead(
      @Payload Map<String, Long> payload, Principal principal) {
    Long conversationId = payload.get(FLAG);
    if (conversationId == null) {
      throw new IllegalArgumentException("Conversation ID is required");
    }

    int count = chatService.markMessagesAsRead(principal.getName(), conversationId);

    return WebSocketUtils.createMessagesMarkedReadResponse(conversationId, count);
  }

  @MessageMapping("/messages.typing")
  public void typingIndicator(@Payload Map<String, Object> payload, Principal principal) {
    String receiverId = (String) payload.get("receiverId");
    boolean isTyping = !payload.containsKey("isTyping") || (boolean) payload.get("isTyping");

    long conversationId;
    if (payload.get(FLAG) != null) {
      conversationId = ((Number) payload.get(FLAG)).longValue();
    } else {
      return;
    }

    String senderId = principal.getName();

    typingIndicatorService.setTypingStatus(senderId, receiverId, conversationId, isTyping);
  }

  @MessageMapping("/users.status")
  @SendToUser("/queue/messages")
  public Map<String, Object> getUserStatus(@Payload Map<String, String> payload) {
    String username = payload.get("username");

    if (username == null) {
      throw new IllegalArgumentException("Username is required");
    }

    boolean isOnline = userStatusService.isUserOnline(username);

    return WebSocketUtils.createUserStatusResponse(username, isOnline);
  }
}
