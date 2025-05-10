package com.altester.chat_service.service.impl;

import com.altester.chat_service.service.UserStatusService;
import com.altester.chat_service.service.WebSocketService;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusServiceImpl implements UserStatusService {
  private final WebSocketService webSocketService;
  private final RedisTemplate<String, String> redisTemplate;

  private static final String USER_STATUS_PREFIX = "user:status:";
  private static final long USER_STATUS_TTL_SECONDS = 180;

  @Override
  public void setUserOnline(String username) {
    String key = getUserStatusKey(username);
    Boolean wasOnline = redisTemplate.hasKey(key);

    redisTemplate.opsForValue().set(key, "online", Duration.ofSeconds(USER_STATUS_TTL_SECONDS));

    if (wasOnline == null || !wasOnline) {
      broadcastUserStatus(username, true);
      log.info("User {} connected and is now online", username);
    }
  }

  @Override
  public void setUserOffline(String username) {
    String key = getUserStatusKey(username);
    Boolean wasOnline = redisTemplate.hasKey(key);

    if (wasOnline != null && wasOnline) {
      redisTemplate.delete(key);

      broadcastUserStatus(username, false);
      log.info("User {} disconnected and is now offline", username);
    }
  }

  @Override
  public boolean isUserOnline(String username) {
    String key = getUserStatusKey(username);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  @Override
  public Set<String> getOnlineUsers() {
    Set<String> keys = redisTemplate.keys(USER_STATUS_PREFIX + "*");
    if (keys == null || keys.isEmpty()) {
      return Set.of();
    }

    return keys.stream().map(this::extractUsernameFromKey).collect(Collectors.toSet());
  }

  private void broadcastUserStatus(String username, boolean isOnline) {
    webSocketService.broadcastUserStatus(username, isOnline);
  }

  private String getUserStatusKey(String username) {
    return USER_STATUS_PREFIX + username;
  }

  private String extractUsernameFromKey(String key) {
    return key.substring(USER_STATUS_PREFIX.length());
  }
}
