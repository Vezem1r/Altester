package com.altester.auth.controller;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.dto.Auth.RegisterUserDTO;
import com.altester.auth.dto.Auth.VerifyUserDTO;
import com.altester.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody RegisterUserDTO registerUserDto){
        log.info("Received register request: {}", registerUserDto);
        authService.register(registerUserDto);
        log.info("User has been created. Verification code has been send on your email");
        return ResponseEntity.ok("User has been created. Verification code has been send on your email");
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signin(@Valid @RequestBody LoginUserDTO loginUserDto) {
        log.info("Received login request: {}", loginUserDto);
        LoginResponse loginResponse = authService.signIn(loginUserDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@Valid @RequestBody VerifyUserDTO verifyUserDto){
        authService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(Map.of("message", "Account verified successfully"));
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendVerificationCode(
            @RequestParam @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email address") String email){
        authService.resendVerificationCode(email);
        return ResponseEntity.ok("Verification code resend");
    }
}
