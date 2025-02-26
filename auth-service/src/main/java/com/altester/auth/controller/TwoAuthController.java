package com.altester.auth.controller;

import com.altester.auth.dto.TwoFaDTO;
import com.altester.auth.dto.VerifyTwoFactorManagement;
import com.altester.auth.models.User;
import com.altester.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
@Slf4j
public class TwoAuthController {

    private final TwoFactorService twoFactorService;

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

    @PostMapping("/switch")
    public ResponseEntity<?> enable2FA(User user) {
        try {
            twoFactorService.manageTwoFactor(user);
            return ResponseEntity.ok("User requests two-factor switching successfully and waits for verification");
        } catch (RuntimeException err) {
            log.error("2FA switching error: {}", err.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", err.getMessage()));
        }
    }

    @PostMapping("/switch/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyTwoFactorManagement management) {
        try {
            twoFactorService.verifyTwoFactorManagement(management.getUserId(), management.getCode());
            return ResponseEntity.ok("User switched two-factor successfully");
        } catch (RuntimeException err) {
            log.error("2FA switch verification error: {}", err.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", err.getMessage()));
        }
    }
}
