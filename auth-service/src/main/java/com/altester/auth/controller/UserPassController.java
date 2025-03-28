package com.altester.auth.controller;

import com.altester.auth.dto.ChangePassDTO;
import com.altester.auth.service.UserPassService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/password")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserPassController {

    private final UserPassService userSecService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam @NotBlank @Email String email){
        log.info("Password reset request received for email: {}", email);
        userSecService.initiatePasswordReset(email);
        return ResponseEntity.ok("Password reset code has been sent to your email.");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@Valid @RequestBody ChangePassDTO changePassDTO) {
        log.info("Password reset confirmation received for email: {}", changePassDTO.getEmail());
        userSecService.resetPassword(changePassDTO);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendPasswordCode(@RequestParam @NotBlank @Email String email){
        log.info("Resend password reset code request received for email: {}", email);
        userSecService.resendResetCode(email);
        return ResponseEntity.ok("Verification code resend");
    }
}
