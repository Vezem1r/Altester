package com.altester.core.controller.auth;

import com.altester.core.config.AppConfig;
import com.altester.core.dtos.auth_service.email.EmailConfirmDTO;
import com.altester.core.dtos.auth_service.email.EmailInitDTO;
import com.altester.core.dtos.auth_service.email.EmailResendDTO;
import com.altester.core.model.auth.User;
import com.altester.core.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserEmailController {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final AppConfig appConfig;

  private static final String API_KEY_HEADER = "x-api-key";

  private User getUserByPrincipal(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(() -> new UsernameNotFoundException(principal.getName()));
  }

  private <T> ResponseEntity<T> forwardRequest(
      String endpoint, HttpMethod method, HttpEntity<Object> requestEntity, Class<T> responseType) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set(API_KEY_HEADER, appConfig.getApiKey());

      if (requestEntity != null) {
        headers.addAll(requestEntity.getHeaders());
        requestEntity = new HttpEntity<>(requestEntity.getBody(), headers);
      } else {
        requestEntity = new HttpEntity<>(headers);
      }

      ResponseEntity<T> responseEntity =
          restTemplate.exchange(
              appConfig.getEmailUrl() + endpoint, method, requestEntity, responseType);
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

  @PostMapping("/request")
  public ResponseEntity<String> requestEmailReset(
      @RequestBody EmailInitDTO emailInitDTO, Principal principal) {
    String username = principal.getName();
    log.info("Forwarding email reset request for email: {}", emailInitDTO.getEmail());

    emailInitDTO.setUsername(username);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> requestEntity = new HttpEntity<>(emailInitDTO, headers);
    return forwardRequest("/request", HttpMethod.POST, requestEntity, String.class);
  }

  @PostMapping("/resend")
  public ResponseEntity<String> resendEmail(
      @RequestBody EmailResendDTO emailResendDTO, Principal principal) {
    String username = principal.getName();
    log.info("Forwarding email resend request for email: {}", emailResendDTO.getEmail());

    emailResendDTO.setUsername(username);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> requestEntity = new HttpEntity<>(emailResendDTO, headers);
    return forwardRequest("/resend", HttpMethod.POST, requestEntity, String.class);
  }

  @PostMapping("/confirm")
  public ResponseEntity<String> confirm(
      @RequestBody EmailConfirmDTO emailConfirmDTO, Principal principal) {
    User user = getUserByPrincipal(principal);
    log.info("Forwarding email confirmation request for email: {}", emailConfirmDTO.getEmail());

    emailConfirmDTO.setUserId(user.getId());
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> requestEntity = new HttpEntity<>(emailConfirmDTO, headers);
    return forwardRequest("/confirm", HttpMethod.POST, requestEntity, String.class);
  }
}
