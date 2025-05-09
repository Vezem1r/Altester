package com.altester.chat_service.security;

import com.altester.chat_service.exception.AuthenticationException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      log.warn("StompHeaderAccessor is null, skipping authentication.");
      return message;
    }

    StompCommand command = accessor.getCommand();
    if (command == null) {
      log.warn("STOMP command is null, skipping authentication.");
      return message;
    }

    if (StompCommand.CONNECT.equals(command)) {
      log.debug("Processing CONNECT command - authenticating user");
      return authenticateUser(message, accessor);
    }

    if (StompCommand.DISCONNECT.equals(command)) {
      log.debug(
          "Processing DISCONNECT command for user: {}",
          accessor.getUser() != null ? accessor.getUser().getName() : "unknown");
      return message;
    }

    if (accessor.getUser() == null) {
      log.debug(
          "No user authentication in session for command: {} - attempting to authenticate",
          command);
      return authenticateUser(message, accessor);
    }

    log.debug(
        "User already authenticated for command {}: {}", command, accessor.getUser().getName());
    return message;
  }

  private Message<?> authenticateUser(Message<?> message, StompHeaderAccessor accessor) {
    List<String> authHeaders = accessor.getNativeHeader("Authorization");

    if (authHeaders == null || authHeaders.isEmpty()) {
      log.warn("Missing Authorization header in WebSocket command: {}", accessor.getCommand());
      throw new AuthenticationException("Missing JWT token in Authorization header") {};
    }

    String authHeader = authHeaders.getFirst();
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("Invalid Authorization header format: {}", authHeader);
      throw new AuthenticationException("Invalid Authorization header") {};
    }

    try {
      String jwt = authHeader.substring(BEARER_PREFIX.length());
      String username = jwtService.extractUsername(jwt);

      if (username == null || username.isBlank()) {
        log.warn("JWT token does not contain a valid username");
        throw new AuthenticationException("Invalid JWT token");
      }

      log.debug("Username extracted from token: {}", username);

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      if (!jwtService.isTokenValid(jwt, userDetails)) {
        log.warn("Invalid or expired JWT token for user: {}", username);
        throw new AuthenticationException("JWT validation failed");
      }

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      accessor.setUser(authToken);

      log.info(
          "WebSocket connection authenticated for user: {} with command: {}",
          username,
          accessor.getCommand());
    } catch (AuthenticationException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "WebSocket authentication error for command {}: {}",
          accessor.getCommand(),
          e.getMessage(),
          e);
      throw new SecurityException("WebSocket authentication failed: " + e.getMessage());
    }

    return message;
  }
}
