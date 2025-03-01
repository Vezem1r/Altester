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

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Forwarding signin request to auth-service for user: {}", loginRequest.getUsernameOrEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<LoginRequestDTO> requestEntity = new HttpEntity<>(loginRequest, headers);

        try {
            ResponseEntity<LoginResponseDTO> responseEntity = restTemplate.exchange(
                    getAuthServiceUrl() + "/signin",
                    HttpMethod.POST,
                    requestEntity,
                    LoginResponseDTO.class
            );
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/ldap/signin")
    public ResponseEntity<?> ldapLogin(@RequestBody LdapLoginDTO ldapLogin) {
        log.info("Forwarding ldap sign in request to auth-service for user: {}", ldapLogin.getLogin());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<LdapLoginDTO> requestEntity = new HttpEntity<>(ldapLogin, headers);

        try {
            ResponseEntity<LoginResponseDTO> responseEntity = restTemplate.exchange(
                    getLdapUrl() + "/login",
                    HttpMethod.POST,
                    requestEntity,
                    LoginResponseDTO.class
            );
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("LDAP Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "LDAP authentication failed"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequestDTO registerRequest) {
        log.info("Forwarding signup request to auth-service for email: {}", registerRequest.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<RegisterRequestDTO> requestEntity = new HttpEntity<>(registerRequest, headers);

        try {
            restTemplate.exchange(
                    getAuthServiceUrl() + "/signup",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
        log.info("Forwarding verification request for user: {}", verifyUserDto.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<VerifyUserDTO> requestEntity = new HttpEntity<>(verifyUserDto, headers);

        try {
            restTemplate.exchange(
                    getAuthServiceUrl() + "/verify",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Account verified successfully"));
        } catch (Exception e) {
            log.error("Verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code or expired"));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        log.info("Forwarding request to resend verification code for email: {}", email);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    getAuthServiceUrl() + "/resend?email=" + email,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Verification code resent"));
        } catch (Exception e) {
            log.error("Resend verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to resend verification code"));
        }
    }
}
