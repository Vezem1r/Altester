package com.altester.auth.controller;

import com.altester.auth.dto.TwoFaDTO;
import com.altester.auth.service.AuthService;
import com.altester.auth.service.JwtService;
import com.altester.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
@Slf4j
public class TwoAuthController {

    private final TwoFactorService twoFactorService;
    private final AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<?> verify2FACode(@RequestBody TwoFaDTO twoFaDTO) {
        try {
            String token = twoFactorService.verifyCode(twoFaDTO.getTwoFactorCode(), twoFaDTO.getEmailOrUsername());
            return ResponseEntity.ok(token);
        } catch (RuntimeException err) {
            log.error("2FA verification error: {}", err.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", err.getMessage()));
        }
    }
}
