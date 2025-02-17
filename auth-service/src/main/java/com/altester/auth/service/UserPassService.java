package com.altester.auth.service;

import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPassService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final EmailUtils emailUtils;


    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() ->{
            log.error("User not found");
            return new RuntimeException("User not found");
                });

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.PASSWORD_RESET);
        if (optionalCode.isPresent()) {
            log.info("Deleting password reset code {}", optionalCode);
            Codes code = optionalCode.get();
            codeRepository.delete(code);
        }

        String resetCode = authService.generateVerificationCode();
        Codes code = new Codes();
        code.setSendAt(LocalDateTime.now());
        code.setUser(user);
        code.setCodeType(CodeType.PASSWORD_RESET);
        code.setCode(resetCode);
        code.setExpiration(LocalDateTime.now().plusMinutes(15));
        codeRepository.save(code);
        emailUtils.sendVerificationEmail(user, EmailType.CHANGE_PASS);
    }

    public void resetPassword(Long userId, String resetCode, String newPassword){
        log.info("Resetting password for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("User not found");
                });

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.PASSWORD_RESET);
        if (optionalCode.isEmpty()) {
            initiatePasswordReset(user.getEmail());
        }

        Codes code = optionalCode.get();

        if (code.getExpiration().isBefore(LocalDateTime.now())) {
            log.error("Reset code has expired for userId: {}", userId);
            throw new RuntimeException("Reset code has expired");
        }

        if (!code.getCode().equals(resetCode)) {
            log.error("Invalid reset code for userId: {}. Provided: {}", userId, resetCode);
            throw new RuntimeException("Invalid reset code");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        codeRepository.delete(code);
        userRepository.save(user);
        log.info("Password reset successful for userId: {}", userId);
    }

    public void resendResetCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(optionalUser.get(), CodeType.PASSWORD_RESET);

            if (optionalCode.isEmpty()) {
                log.error("Resend code not found for user: {}", email);
                throw new RuntimeException("Code not found");
            }

            Codes code = optionalCode.get();

            if (code.getSendAt().plusSeconds(59).isAfter(LocalDateTime.now())) {
                log.warn("Password reset code was sent less than a minute ago for user: {}", email);
                throw new RuntimeException("Password reset code was sent less than a minute ago");
            }

            code.setCode(authService.generateVerificationCode());
            code.setExpiration(LocalDateTime.now().plusMinutes(15));
            code.setSendAt(LocalDateTime.now());
            codeRepository.save(code);
            emailUtils.sendVerificationEmail(user, EmailType.CHANGE_PASS);
            log.info("Resent password reset code to user: {}", email);
        } else {
            log.error("User not found for resending password reset code: {}", email);
            throw new RuntimeException("User not found");
        }
    }
}
