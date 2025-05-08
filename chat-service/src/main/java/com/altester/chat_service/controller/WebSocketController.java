package com.altester.chat_service.controller;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.MessageRequest;
import com.altester.chat_service.model.User;
import com.altester.chat_service.repository.GroupRepository;
import com.altester.chat_service.service.ChatService;
import com.altester.chat_service.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final ChatService chatService;
    private final WebSocketService webSocketService;
    private final GroupRepository groupRepository;

    @MessageMapping("/messages.connect")
    @SendToUser("/queue/messages")
    public Map<String, Object> handleConnection(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            log.error("No authenticated user found in message headers");
            throw new SecurityException("Authentication required");
        }

        String username = user.getName();
        log.info("WebSocket connection established for user: {}", username);

        List<ChatMessageDTO> unreadMessages = chatService.getUnreadMessages(username);

        Map<String, Object> availableUsers = new HashMap<>();
        availableUsers.put("users", getAvailableUsersForChat(username));

        log.info("Sending initial data to user {}: {} unread messages",
                username, unreadMessages.size());

        return Map.of(
                "type", "INITIAL_DATA",
                "unreadMessages", unreadMessages,
                "conversations", chatService.getConversationsForUser(username),
                "availableUsers", availableUsers
        );
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
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("username", user.getUsername());
                    userMap.put("role", user.getRole());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @MessageMapping("/messages.send")
    @SendToUser("/queue/messages")
    public Map<String, Object> sendMessage(@Payload MessageRequest messageRequest, Principal principal) {
        log.debug("Received message from {} to {}: {}",
                principal.getName(), messageRequest.getReceiverId(), messageRequest.getContent());

        ChatMessageDTO message = chatService.sendMessage(principal.getName(), messageRequest);

        return Map.of(
                "type", "MESSAGE_SENT",
                "message", message
        );
    }

    @MessageMapping("/messages.markRead")
    @SendToUser("/queue/messages")
    public Map<String, Object> markMessagesAsRead(@Payload Map<String, Long> payload, Principal principal) {
        Long conversationId = payload.get("conversationId");
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID is required");
        }

        int count = chatService.markMessagesAsRead(principal.getName(), conversationId);

        return Map.of(
                "type", "MESSAGES_MARKED_READ",
                "conversationId", conversationId,
                "markedCount", count
        );
    }

    @MessageMapping("/messages.typing")
    public void typingIndicator(@Payload Map<String, Object> payload, Principal principal) {
        String receiverId = (String) payload.get("receiverId");
        Long conversationId = ((Number) payload.get("conversationId")).longValue();

        webSocketService.sendTypingIndicator(
                receiverId,
                principal.getName(),
                conversationId,
                true
        );
    }
}