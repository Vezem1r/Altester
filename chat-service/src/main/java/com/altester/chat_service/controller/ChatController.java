package com.altester.chat_service.controller;

import com.altester.chat_service.dto.ChatMessageDTO;
import com.altester.chat_service.dto.ConversationDTO;
import com.altester.chat_service.dto.MessageRequest;
import com.altester.chat_service.service.ChatService;
import com.altester.chat_service.service.UserStatusService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

  private final ChatService chatService;
  private final UserStatusService userStatusService;

  @GetMapping("/conversations")
  public ResponseEntity<Page<ConversationDTO>> getConversations(
      Principal principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        chatService.getPaginatedConversations(principal.getName(), page, size));
  }

  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<Page<ChatMessageDTO>> getConversationMessages(
      Principal principal,
      @PathVariable Long conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        chatService.getConversationMessages(principal.getName(), conversationId, page, size));
  }

  @PutMapping("/conversations/{conversationId}/read")
  public ResponseEntity<Integer> markConversationAsRead(
      Principal principal, @PathVariable Long conversationId) {
    int count = chatService.markMessagesAsRead(principal.getName(), conversationId);
    return ResponseEntity.ok(count);
  }

  @PostMapping("/messages")
  public ResponseEntity<ChatMessageDTO> sendMessage(
      Principal principal, @Valid @RequestBody MessageRequest messageRequest) {
    ChatMessageDTO message = chatService.sendMessage(principal.getName(), messageRequest);
    return ResponseEntity.ok(message);
  }

  @GetMapping("/messages/unread/count")
  public ResponseEntity<Map<String, Integer>> getUnreadCount(Principal principal) {
    List<ChatMessageDTO> unread = chatService.getUnreadMessages(principal.getName());
    return ResponseEntity.ok(Map.of("count", unread.size()));
  }

  @GetMapping("/conversations/{conversationId}/first-unread")
  public ResponseEntity<Map<String, Long>> getFirstUnreadMessageId(
      Principal principal, @PathVariable Long conversationId) {
    Long messageId = chatService.getFirstUnreadMessageId(principal.getName(), conversationId);
    return ResponseEntity.ok(Map.of("messageId", messageId != null ? messageId : -1L));
  }

  @GetMapping("/users/online")
  public ResponseEntity<List<String>> getOnlineUsers() {
    return ResponseEntity.ok(List.copyOf(userStatusService.getOnlineUsers()));
  }
}
