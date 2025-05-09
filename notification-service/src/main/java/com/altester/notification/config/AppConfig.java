package com.altester.notification.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {
  @Value("${INTERNAL_API_KEY}")
  private String apiKey;

  @Value("${security.jwt.secret-key}")
  @Getter
  private String secretKey;
}
