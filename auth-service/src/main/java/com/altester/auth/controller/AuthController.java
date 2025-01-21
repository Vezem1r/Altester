package com.altester.auth.controller;

import com.altester.auth.dto.LoginResponse;
import com.altester.auth.dto.LoginUserDTO;
import com.altester.auth.dto.RegisterUserDTO;
import com.altester.auth.dto.VerifyUserDTO;
import com.altester.auth.models.User;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.AuthService;
import com.altester.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody RegisterUserDTO registerUserDto){
        log.info("Received register request: {}", registerUserDto);

        User registeredUser = authService.signUp(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signin(@RequestBody LoginUserDTO loginUserDto){
        log.info("Received login request: {}", loginUserDto);

        User authenticatedUser = authService.signIn(loginUserDto);
        authenticatedUser.setLastLogin(LocalDateTime.now());
        userRepository.save(authenticatedUser);
        String jwtToken = jwtService.generateToken(authenticatedUser, authenticatedUser.getRole().name());
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime(), "Login successful");

        String role = jwtService.extractRole(jwtToken);
        log.info("Extracted role: {}", role);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto){
        try {
            authService.verifyUser(verifyUserDto);
            return ResponseEntity.ok(Map.of("message", "Account verified successfully")); // Return as JSON
        } catch (RuntimeException err) {
            log.error("Verification error: {}", err.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", err.getMessage()));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try{
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code resend");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
