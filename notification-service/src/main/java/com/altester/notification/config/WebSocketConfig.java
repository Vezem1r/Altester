package com.altester.notification.config;

import com.altester.notification.security.JwtWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtWebSocketInterceptor jwtInterceptor;
  private static final int SEND_TIME_LIMIT = 20 * 1000;
  private static final int SEND_BUFFER_SIZE = 512 * 1024;
  private static final int MESSAGE_SIZE_LIMIT = 128 * 1024;
  private final AppConfig appConfig;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/queue");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOrigins(appConfig.getAllowedOrigins().toArray(new String[0]))
        .withSockJS();
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration
        .setSendTimeLimit(SEND_TIME_LIMIT)
        .setSendBufferSizeLimit(SEND_BUFFER_SIZE)
        .setMessageSizeLimit(MESSAGE_SIZE_LIMIT);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(jwtInterceptor);
  }
}
