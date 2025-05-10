package com.altester.core.controller.auth;

import com.altester.core.config.AppConfig;
import com.altester.core.config.AuthConfigProperties;
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
  public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
    if (!authConfigProperties.isStandardAuthEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    log.info(
        "Forwarding signin request to auth-service for user: {}",
        loginRequest.getUsernameOrEmail());
    return forwardRequest(
        appConfig.getAuthUrl() + "/signin", HttpMethod.POST, loginRequest, LoginResponseDTO.class);
  }

  @PostMapping("/ldap/signin")
  public ResponseEntity<LoginResponseDTO> ldapLogin(@RequestBody LdapLoginDTO ldapLogin) {
    if (!authConfigProperties.isLdapAuthEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    log.info("Forwarding LDAP signin request to auth-service for user: {}", ldapLogin.getLogin());
    return forwardRequest(
        appConfig.getLdapUrl() + "/login", HttpMethod.POST, ldapLogin, LoginResponseDTO.class);
  }

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody RegisterRequestDTO registerRequest) {
    if (!authConfigProperties.isRegistrationEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Registration is currently disabled");
    }

    log.info("Forwarding signup request to auth-service for email: {}", registerRequest.getEmail());
    return forwardRequest(
        appConfig.getAuthUrl() + "/signup", HttpMethod.POST, registerRequest, String.class);
  }

  @PostMapping("/verify")
  public ResponseEntity<String> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
    if (!authConfigProperties.isRegistrationEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Registration verification is currently disabled");
    }

    log.info("Forwarding verification request for user: {}", verifyUserDto.getEmail());
    return forwardRequest(
        appConfig.getAuthUrl() + "/verify", HttpMethod.POST, verifyUserDto, String.class);
  }

  @PostMapping("/resend")
  public ResponseEntity<String> resendVerificationCode(@RequestParam String email) {
    if (!authConfigProperties.isRegistrationEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Registration verification is currently disabled");
    }

    log.info("Forwarding request to resend verification code for email: {}", email);
    return forwardRequest(
        appConfig.getAuthUrl() + "/resend?email=" + email, HttpMethod.POST, null, String.class);
  }
}
