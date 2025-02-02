package com.altester.auth.service;

import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final EmailService emailService;
    private final CodeRepository codeRepository;
    private final TemplateEngine templateEngine;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    public void send2FACode(User user) {

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.TWO_FACTOR);
        if (optionalCode.isEmpty()) {
            log.error("There is no 2FA code for user: {}", user);
            throw new RuntimeException("There is no 2FA code for user: " + user);
        }

        Codes code = optionalCode.get();
        Context context = new Context();
        context.setVariable("twofactor", code.getCode());
        context.setVariable("expiration", code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        context.setVariable("year", LocalDate.now().getYear());

        String htmlMessage = templateEngine.process("twoFactor", context);

        String subject = "2FA Verification";

        try {
            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
            log.info("2FA email sent");
        } catch (MessagingException e) {
            log.error("Failed to send 2FA email to: {}", user.getEmail(), e);
        }
    }

    public String verifyCode(String twoFactorCode, String emailOrUsername) {

        log.info("Attempting to login user: {}", emailOrUsername);

        Optional<User> optionalUser;

        if (emailOrUsername.contains("@")) {
            log.info("Attempting to login user with email: {}", emailOrUsername);
            optionalUser = userRepository.findByEmail(emailOrUsername);
        } else {
            log.info("Attempting to login user with username: {}", emailOrUsername);
            optionalUser = userRepository.findByUsername(emailOrUsername);
        }

        if (optionalUser.isEmpty()) {
            log.error("User with email '{}' not found.", emailOrUsername);
            throw new UsernameNotFoundException("User " + emailOrUsername + " not found.");
        }

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(optionalUser.get(), CodeType.TWO_FACTOR);
        if (optionalCode.isEmpty()) {
            log.error("2FA Code not found for user: {}", emailOrUsername);
            throw new RuntimeException("2FA Code not found");
        }

        User user = optionalUser.get();
        Codes code = optionalCode.get();

        if (code.getExpiration().isBefore(LocalDateTime.now())) {
            log.error("2FA code has expired for user: {}", emailOrUsername);
            throw new RuntimeException("2FA code has expired");
        }

        if (code.getCode().equals(twoFactorCode)) {
            codeRepository.delete(code);
            String token = jwtService.generateToken(user, user.getRole().name());
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.info("User logged in via 2FA successfully: {}", emailOrUsername);
            return token;
        } else {
            log.error("Invalid 2FA code for user: {}", emailOrUsername);
            throw new RuntimeException("Invalid 2FA code");
        }
    }
}
