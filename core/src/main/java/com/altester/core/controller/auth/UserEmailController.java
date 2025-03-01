package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.email.EmailConfirmDTO;
import com.altester.core.dtos.auth_service.email.EmailInitDTO;
import com.altester.core.dtos.auth_service.email.EmailResendDTO;
import com.altester.core.model.auth.User;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Map;

@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserEmailController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    private String getAuthServiceUrl() {
        return authServiceUrl + "/email";
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestEmailReset(@RequestBody EmailInitDTO emailInitDTO, Principal principal) {
        String username = principal.getName();

        log.info("Forwarding email reset request to auth-service for email: {}", emailInitDTO.getEmail());
        log.info("Request initiated by user: {}", username);

        emailInitDTO.setUsername(username);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailInitDTO> requestEntity = new HttpEntity<>(emailInitDTO, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    getAuthServiceUrl() + "/request",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Error requesting email reset: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to send email reset request: " + e.getMessage()));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendEmail(@RequestBody EmailResendDTO emailResendDTO, Principal principal) {
        String username = principal.getName();

        log.info("Forwarding email resend request for user email change to auth-service for email: {}", emailResendDTO.getEmail());
        log.info("Request initiated by user: {}", username);

        emailResendDTO.setUsername(username);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailResendDTO> requestEntity = new HttpEntity<>(emailResendDTO, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    getAuthServiceUrl() + "/resend",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Error resending email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to resend email: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody EmailConfirmDTO emailConfirmDTO, Principal principal) {
        String username = principal.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        log.info("Forwarding email confirm request to auth-service for email: {}", emailConfirmDTO.getEmail());
        log.info("Request initiated by user: {}", username);

        emailConfirmDTO.setUserId(user.getId());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailConfirmDTO> requestEntity = new HttpEntity<>(emailConfirmDTO, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    getAuthServiceUrl() + "/confirm",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Error confirming email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to confirm email: " + e.getMessage()));
        }
    }
}
