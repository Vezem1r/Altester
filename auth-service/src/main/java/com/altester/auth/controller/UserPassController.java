package com.altester.auth.controller;

import com.altester.auth.dto.ChangePassDTO;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.UserPassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/password")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserPassController {

    private final UserPassService userSecService;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

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
        Optional<Codes> optionalCode = codeRepository.findByCodeAndCodeType(changePassDTO.getResetCode(), CodeType.PASSWORD_RESET);
        if (optionalCode.isEmpty()) {
            log.error("Password reset code not found: {}", optionalCode);
            throw new RuntimeException("Code not found");
        }

        Codes code = optionalCode.get();

        Optional<User> optionalUser = userRepository.findById(code.getUser().getId());
        if (optionalUser.isEmpty()) {
            log.error("User not found during password reset confirmation: {}", optionalUser);
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();

        userSecService.resetPassword(user.getId(), changePassDTO.getResetCode(), changePassDTO.getNewPassword());

        return ResponseEntity.ok("Password has been successfully reset.");
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
