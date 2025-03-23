package com.altester.core.controller.auth;

import com.altester.core.config.AuthConfigProperties;
import com.altester.core.dtos.auth_service.auth.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;
    private final AuthConfigProperties authConfigProperties;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    private String getAuthServiceUrl() {
        return authServiceUrl + "/auth";
    }

    private String getLdapUrl() {
        return authServiceUrl + "/ldap";
    }

    private ResponseEntity<?> forwardRequest(String url, HttpMethod method, Object body, Class<?> responseType) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<?> responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Request error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientResponseException e) {
            log.error("RestClientResponseException: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
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
        if (!authConfigProperties.isStandardAuthEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Standard authentication is currently disabled");
        }

        log.info("Forwarding signin request to auth-service for user: {}", loginRequest.getUsernameOrEmail());
        return forwardRequest(getAuthServiceUrl() + "/signin", HttpMethod.POST, loginRequest, LoginResponseDTO.class);
    }

    @PostMapping("/ldap/signin")
    public ResponseEntity<?> ldapLogin(@RequestBody LdapLoginDTO ldapLogin) {
        if (!authConfigProperties.isLdapAuthEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("LDAP authentication is currently disabled");
        }

        log.info("Forwarding LDAP signin request to auth-service for user: {}", ldapLogin.getLogin());
        return forwardRequest(getLdapUrl() + "/login", HttpMethod.POST, ldapLogin, LoginResponseDTO.class);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequestDTO registerRequest) {
        if (!authConfigProperties.isRegistrationEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Registration is currently disabled");
        }

        log.info("Forwarding signup request to auth-service for email: {}", registerRequest.getEmail());
        return forwardRequest(getAuthServiceUrl() + "/signup", HttpMethod.POST, registerRequest, String.class);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
        if (!authConfigProperties.isRegistrationEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Registration verification is currently disabled");
        }

        log.info("Forwarding verification request for user: {}", verifyUserDto.getEmail());
        return forwardRequest(getAuthServiceUrl() + "/verify", HttpMethod.POST, verifyUserDto, String.class);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        if (!authConfigProperties.isRegistrationEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Registration verification is currently disabled");
        }

        log.info("Forwarding request to resend verification code for email: {}", email);
        return forwardRequest(getAuthServiceUrl() + "/resend?email=" + email, HttpMethod.POST, null, String.class);
    }
}