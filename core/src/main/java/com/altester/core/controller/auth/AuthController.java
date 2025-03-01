package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.auth.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    private String getAuthServiceUrl() {
        return authServiceUrl + "/auth";
    }

    private String getLdapUrl() {
        return authServiceUrl + "/ldap";
    }

    private ResponseEntity<?> forwardRequest(String url, HttpMethod method, Object body) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Request error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Request failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Forwarding signin request to auth-service for user: {}", loginRequest.getUsernameOrEmail());
        return forwardRequest(getAuthServiceUrl() + "/signin", HttpMethod.POST, loginRequest);
    }

    @PostMapping("/ldap/signin")
    public ResponseEntity<?> ldapLogin(@RequestBody LdapLoginDTO ldapLogin) {
        log.info("Forwarding ldap signin request to auth-service for user: {}", ldapLogin.getLogin());
        return forwardRequest(getLdapUrl() + "/login", HttpMethod.POST, ldapLogin);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequestDTO registerRequest) {
        log.info("Forwarding signup request to auth-service for email: {}", registerRequest.getEmail());
        return forwardRequest(getAuthServiceUrl() + "/signup", HttpMethod.POST, registerRequest);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
        log.info("Forwarding verification request for user: {}", verifyUserDto.getEmail());
        return forwardRequest(getAuthServiceUrl() + "/verify", HttpMethod.POST, verifyUserDto);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        log.info("Forwarding request to resend verification code for email: {}", email);
        return forwardRequest(getAuthServiceUrl() + "/resend?email=" + email, HttpMethod.POST, null);
    }
}
