package com.altester.chat_service.controller;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.exception.AuthenticationException;
import com.altester.chat_service.model.User;
import com.altester.chat_service.repository.GroupRepository;
import com.altester.chat_service.service.ChatService;
import com.altester.chat_service.util.WebSocketUtils;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

  private final ChatService chatService;
  private final GroupRepository groupRepository;

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

    List<ChatMessageDTO> unreadMessages = chatService.getUnreadMessages(username);

    Map<String, Object> availableUsers = new HashMap<>();
    availableUsers.put("users", getAvailableUsersForChat(username));

    log.info(
        "Sending initial data to user {}: {} unread messages", username, unreadMessages.size());

    return WebSocketUtils.createInitialDataResponse(
        unreadMessages, chatService.getConversationsForUser(username), availableUsers);
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
              userMap.put("online", false);
              return userMap;
            })
        .collect(Collectors.toList());
  }
}
