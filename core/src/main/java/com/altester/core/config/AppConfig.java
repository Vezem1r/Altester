package com.altester.core.config;

import com.altester.core.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

  private final UserRepository userRepository;
  private final DemoConfig demoConfig;

  @Value("${AUTH_SERVICE_URL}")
  private String authServiceUrl;

  @Value("${INTERNAL_API_KEY}")
  @Getter
  private String apiKey;

  @Value("${api-key.encryption.secret}")
  @Getter
  private String secretKey;

  @Value("${NOTIFICATION_SERVICE_URL}")
  private String notificationServiceUrl;

  @Value("${AI_GRADING_SERVICE_URL}")
  private String aiGradingServiceUrl;

  public String getAuthUrl() {
    return authServiceUrl + "/auth";
  }

  public String getLdapUrl() {
    return authServiceUrl + "/ldap";
  }

  public String getEmailUrl() {
    return authServiceUrl + "/email";
  }

  public String getPasswordUrl() {
    return authServiceUrl + "/password";
  }

  public String getNotificationUrl() {
    return notificationServiceUrl + "/internal/notifications";
  }

  public String getGraderUrl() {
    return aiGradingServiceUrl + "/ai/grade";
  }

  @Bean
  UserDetailsService userDetailsService() {
    return usernameOrEmail -> {
      if (!isDemoUserAllowed(usernameOrEmail)) {
        String allowedUsersStr = String.join(", ", demoConfig.getAllowedUsers());
        throw new UsernameNotFoundException(
            "DEMO MODE: User '"
                + usernameOrEmail
                + "' is not allowed. "
                + "Available demo users: "
                + allowedUsersStr);
      }

      if (usernameOrEmail.contains("@")) {
        return userRepository
            .findByEmail(usernameOrEmail)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "Demo user not found with email: " + usernameOrEmail));
      } else {
        return userRepository
            .findByUsername(usernameOrEmail)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "Demo user not found with username: " + usernameOrEmail));
      }
    };
  }

  private boolean isDemoUserAllowed(String usernameOrEmail) {
    return demoConfig.getAllowedUsers().stream()
        .anyMatch(allowedUser -> allowedUser.equalsIgnoreCase(usernameOrEmail));
  }

  @Bean
  BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
