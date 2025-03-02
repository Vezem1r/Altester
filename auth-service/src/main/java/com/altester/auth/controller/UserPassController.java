package com.altester.auth.controller;

import com.altester.auth.dto.ChangePassDTO;
import com.altester.auth.service.UserPassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/password")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserPassController {

    private final UserPassService userSecService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam("email") String email){
        try {
            userSecService.initiatePasswordReset(email);
            return ResponseEntity.ok("Password reset code has been sent to your email.");
        } catch (RuntimeException e) {
            log.error("Error sending password reset code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody ChangePassDTO changePassDTO) {
        try {
            userSecService.resetPassword(changePassDTO.getUserId(), changePassDTO.getResetCode(), changePassDTO.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (RuntimeException e) {
            log.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendPasswordCode(@RequestParam String email){
        try{
            userSecService.resendResetCode(email);
            return ResponseEntity.ok("Verification code resend");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
