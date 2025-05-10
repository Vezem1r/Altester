package com.altester.chat_service.service.impl;

import com.altester.chat_service.service.TypingIndicatorService;
import com.altester.chat_service.service.WebSocketService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypingIndicatorServiceImpl implements TypingIndicatorService {

  private final RedisTemplate<String, String> redisTemplate;
  private final WebSocketService webSocketService;

  private static final long TYPING_TIMEOUT_SECONDS = 6;

  private String generateKey(String senderId, String receiverId, Long conversationId) {
    return String.format("typing:%s:%s:%d", senderId, receiverId, conversationId);
  }

  @Override
  public void setTypingStatus(
      String senderId, String receiverId, Long conversationId, boolean isTyping) {
    String key = generateKey(senderId, receiverId, conversationId);

    if (isTyping) {
      redisTemplate.opsForValue().set(key, "typing", TYPING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      webSocketService.sendTypingIndicator(receiverId, senderId, conversationId, true);

      log.debug(
          "Setup typing status for {} to {} in conversation {} (Timeout in {} seconds)",
          senderId,
          receiverId,
          conversationId,
          TYPING_TIMEOUT_SECONDS);
    } else {
      redisTemplate.delete(key);
      webSocketService.sendTypingIndicator(receiverId, senderId, conversationId, false);

      log.debug(
          "Flush typing status for {} to {} in conversation {}",
          senderId,
          receiverId,
          conversationId);
    }
  }
}
