package com.altester.chat_service.service;

import com.altester.chat_service.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendChatMessage(String username, ChatMessageDTO message) {
        log.info("Sending chat message to user: {}", username);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/messages",
                message
        );
    }

    public void sendUnreadCount(String username, Long conversationId, long count) {
        log.info("Sending unread count for conversation {}: {} to user: {}",
                conversationId, count, username);

        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", conversationId);
        response.put("unreadCount", count);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/messages/unread",
                response
        );
    }
}