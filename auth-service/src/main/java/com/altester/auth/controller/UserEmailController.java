package com.altester.auth.controller;

import com.altester.auth.dto.EmailConfirmDTO;
import com.altester.auth.dto.EmailInitDTO;
import com.altester.auth.dto.EmailResendDTO;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.UserEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserEmailController {

    private final UserEmailService userEmailService;

    @PostMapping("/request")
    public ResponseEntity<String> requestEmailReset(@RequestBody EmailInitDTO emailInitDTO) {
        try {
            userEmailService.initiateEmailReset(emailInitDTO.getEmail(), emailInitDTO.getPassword(), emailInitDTO.getUsername());
            return ResponseEntity.ok("Email reset code has been sent to your email.");
        } catch (RuntimeException e) {
            log.error("Error sending email reset code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendPasswordCode(@RequestBody EmailResendDTO emailResendDTO){
        try{
            userEmailService.resendMailCode(emailResendDTO.getEmail(), emailResendDTO.getUsername());
            return ResponseEntity.ok("Email change code resend");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody EmailConfirmDTO emailConfirmDTO) {
        try{
            userEmailService.resetEmail(emailConfirmDTO.getUserId(), emailConfirmDTO.getEmailCode(), emailConfirmDTO.getEmail());
            return ResponseEntity.ok("Email has been reset successfully.");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

