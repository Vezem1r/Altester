package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.email.EmailInitDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
}
