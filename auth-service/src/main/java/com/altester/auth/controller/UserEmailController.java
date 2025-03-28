package com.altester.auth.controller;

import com.altester.auth.dto.EmailConfirmDTO;
import com.altester.auth.dto.EmailInitDTO;
import com.altester.auth.dto.EmailResendDTO;
import com.altester.auth.service.UserEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserEmailController {

    private final UserEmailService userEmailService;

    @PostMapping("/request")
    public ResponseEntity<String> requestEmailReset(@Valid @RequestBody EmailInitDTO emailInitDTO) {
        userEmailService.initiateEmailReset(emailInitDTO);
        return ResponseEntity.ok("Email reset code has been sent to your email.");
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendPasswordCode(@Valid @RequestBody EmailResendDTO emailResendDTO){
        userEmailService.resendMailCode(emailResendDTO);
        return ResponseEntity.ok("Email change code resend");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@Valid @RequestBody EmailConfirmDTO emailConfirmDTO) {
        userEmailService.resetEmail(emailConfirmDTO);
        return ResponseEntity.ok("Email has been reset successfully.");
    }
}

