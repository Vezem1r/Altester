package com.altester.auth.service;

import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPassService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final SpringTemplateEngine templateEngine;
    private final EmailService emailService;


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
        sendResetEmail(user);
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
            sendResetEmail(user);
            log.info("Resent password reset code to user: {}", email);
        } else {
            log.error("User not found for resending password reset code: {}", email);
            throw new RuntimeException("User not found");
        }
    }

    private void sendResetEmail(User user) {
        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.PASSWORD_RESET);
        if (optionalCode.isEmpty()) {
            log.error("Nothing to send for user: {}", user.getUsername());
            throw new RuntimeException("Password reset code not found");
        }

        Codes code = optionalCode.get();
        Context context = new Context();
        context.setVariable("resetCode", code.getCode());
        context.setVariable("expiration", code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        context.setVariable("year", LocalDate.now().getYear());

        String htmlMessage = templateEngine.process("password-reset-email", context);

        String subject = "Password reset code";
        try {
            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }
}
