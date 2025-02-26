package com.altester.core.controller.auth;

import com.altester.core.dtos.auth.LoginRequestDTO;
import com.altester.core.dtos.auth.LoginResponseDTO;
import com.altester.core.dtos.auth.RegisterRequestDTO;
import com.altester.core.dtos.auth.VerifyUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/signin")
    public ResponseEntity<LoginResponseDTO> signin(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Forwarding signin request to auth-service for user: {}", loginRequest.getUsernameOrEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<LoginRequestDTO> requestEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<LoginResponseDTO> responseEntity = restTemplate.exchange(
                getAuthServiceUrl() + "/signin",
                HttpMethod.POST,
                requestEntity,
                LoginResponseDTO.class
        );

        return ResponseEntity.ok(responseEntity.getBody());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody RegisterRequestDTO registerRequest) {
        log.info("Forwarding signup request to auth-service for email: {}", registerRequest.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<RegisterRequestDTO> requestEntity = new HttpEntity<>(registerRequest, headers);

        restTemplate.exchange(
                getAuthServiceUrl() + "/signup",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@RequestParam String email) {
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
