package com.altester.core.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "demo")
public class DemoConfig {

  private String message = "This is a demo version with limited functionality";

  private final List<String> allowedUsers = List.of("admin", "teacher", "student");

  public boolean isEnabled() {
    return true;
  }
}
