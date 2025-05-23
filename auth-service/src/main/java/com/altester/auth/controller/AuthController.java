package com.altester.auth.controller;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signin")
  public ResponseEntity<LoginResponse> signin(@Valid @RequestBody LoginUserDTO loginUserDto) {
    log.info("Received login request: {}", loginUserDto);
    LoginResponse loginResponse = authService.signIn(loginUserDto);
    return ResponseEntity.ok(loginResponse);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("healthy");
  }
}
