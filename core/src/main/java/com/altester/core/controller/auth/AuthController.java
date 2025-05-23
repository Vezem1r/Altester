package com.altester.core.controller.auth;

import com.altester.core.config.AppConfig;
import com.altester.core.config.AuthConfigProperties;
import com.altester.core.config.DemoConfig;
import com.altester.core.dtos.auth_service.auth.*;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RestTemplate restTemplate;
  private final AuthConfigProperties authConfigProperties;
  private final AppConfig appConfig;
  private final DemoConfig demoConfig;

  private static final String API_KEY_HEADER = "x-api-key";

  private <T> ResponseEntity<T> forwardRequest(
      String url, HttpMethod method, Object body, Class<T> responseType) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(API_KEY_HEADER, appConfig.getApiKey());
    HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<T> responseEntity =
          restTemplate.exchange(url, method, requestEntity, responseType);
      return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error("Request error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      @SuppressWarnings("unchecked")
      T errorBody = (T) e.getResponseBodyAsString();
      return ResponseEntity.status(e.getStatusCode()).body(errorBody);
    } catch (RestClientResponseException e) {
      log.error(
          "RestClientResponseException: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      @SuppressWarnings("unchecked")
      T errorBody = (T) e.getResponseBodyAsString();
      return ResponseEntity.status(e.getStatusCode()).body(errorBody);
    } catch (Exception e) {
      log.error("Unexpected error: {}", e.getMessage());
      @SuppressWarnings("unchecked")
      T errorBody = (T) ("Internal server error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
  }

  @GetMapping("/validate-token")
  public ResponseEntity<Map<String, Object>> validateToken() {
    Map<String, Object> response = new HashMap<>();
    response.put("valid", true);
    response.put("message", "Token is valid");

    return ResponseEntity.ok(response);
  }

  @GetMapping("/config")
  public ResponseEntity<Map<String, Object>> getAuthConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("mode", authConfigProperties.getMode().name());
    config.put("standardAuthEnabled", authConfigProperties.isStandardAuthEnabled());
    config.put("registrationEnabled", authConfigProperties.isRegistrationEnabled());
    config.put("ldapAuthEnabled", authConfigProperties.isLdapAuthEnabled());

    return ResponseEntity.ok(config);
  }

  @PostMapping("/signin")
  public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
    if (!isDemoUserAllowed(loginRequest.getUsernameOrEmail())) {
      log.warn(
          "DEMO MODE: Login attempt with non-demo user: {}", loginRequest.getUsernameOrEmail());

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "DEMO MODE: Access denied");
      errorResponse.put(
          "message",
          "Only demo users are allowed: " + String.join(", ", demoConfig.getAllowedUsers()));
      errorResponse.put("demoMode", true);
      errorResponse.put("allowedUsers", demoConfig.getAllowedUsers());

      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    log.info("DEMO MODE: Signin request for demo user: {}", loginRequest.getUsernameOrEmail());

    return forwardRequest(
        appConfig.getAuthUrl() + "/signin", HttpMethod.POST, loginRequest, LoginResponseDTO.class);
  }

  private boolean isDemoUserAllowed(String usernameOrEmail) {
    return demoConfig.getAllowedUsers().stream()
        .anyMatch(allowedUser -> allowedUser.equalsIgnoreCase(usernameOrEmail));
  }
}
