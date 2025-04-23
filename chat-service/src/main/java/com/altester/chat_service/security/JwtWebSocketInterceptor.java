package com.altester.chat_service.security;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.warn("StompHeaderAccessor is null, skipping authentication.");
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            log.warn("STOMP command is null, skipping authentication.");
            return message;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Authentication already exists for command: {}", command);
            return message;
        }

        if (StompCommand.CONNECT.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            log.debug("Processing {} command", command);

            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                log.warn("Missing Authorization header in WebSocket request for command: {}", command);
                throw new SecurityException("Missing JWT token in Authorization header");
            }

            String authHeader = authHeaders.getFirst();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format: {}", authHeader);
                throw new SecurityException("Invalid Authorization header");
            }

            try {
                String jwt = authHeader.substring(7);
                String username = jwtService.extractUsername(jwt);

                if (username == null || username.isBlank()) {
                    log.warn("JWT token does not contain a valid username");
                    throw new SecurityException("Invalid JWT token");
                }

                log.debug("Username extracted from token: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    log.warn("Invalid or expired JWT token for user: {}", username);
                    throw new SecurityException("JWT validation failed");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
                accessor.setUser(authToken);

                log.info("WebSocket connection authenticated for user: {} with command: {}", username, command);

            } catch (Exception e) {
                log.error("WebSocket authentication error for command {}: {}", command, e.getMessage(), e);
                throw new SecurityException("WebSocket authentication failed: " + e.getMessage());
            }
        }
        return message;
    }
}