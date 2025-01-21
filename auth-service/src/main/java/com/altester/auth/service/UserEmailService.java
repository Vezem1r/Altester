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
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserEmailService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CodeRepository codeRepository;
    private final AuthService authService;
    private final TemplateEngine templateEngine;

    public void initiateEmailReset(String email, String password, String username) {

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found");
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Invalid password provided for username: {}", username);
            throw new RuntimeException("Invalid password");
        }

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.EMAIL_CHANGE);
        if (optionalCode.isPresent()) {
            log.info("Deleting email change code {}", optionalCode);
            Codes code = optionalCode.get();
            codeRepository.delete(code);
        }

        String resetCode = authService.generateVerificationCode();
        Codes code = new Codes();
        code.setSendAt(LocalDateTime.now());
        code.setUser(user);
        code.setCodeType(CodeType.EMAIL_CHANGE);
        code.setCode(resetCode);
        code.setExpiration(LocalDateTime.now().plusMinutes(15));
        codeRepository.save(code);
        sendEmailChangeCode(user, email);
    }

    public void resendMailCode(String email, String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(optionalUser.get(), CodeType.EMAIL_CHANGE);

            if (optionalCode.isEmpty()) {
                log.error("Email change code not found for user: {}", user.getEmail());
                throw new RuntimeException("Email change code not found");
            }

            Codes code = optionalCode.get();

            if (code.getSendAt().plusSeconds(59).isAfter(LocalDateTime.now())) {
                log.warn("Email change code was sent less than a minute ago for user: {}", user.getEmail());
                throw new RuntimeException("Email change code was sent less than a minute ago");
            }

            code.setCode(authService.generateVerificationCode());
            code.setExpiration(LocalDateTime.now().plusMinutes(15));
            code.setSendAt(LocalDateTime.now());
            codeRepository.save(code);
            sendEmailChangeCode(user, email);
            log.info("Resent email change code to user: {}", user.getEmail());
        } else {
            log.error("User not found for resending email change code: {}", email);
            throw new RuntimeException("User not found");
        }
    }

    private void sendEmailChangeCode(User user, String email) {
        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.EMAIL_CHANGE);
        if (optionalCode.isEmpty()) {
            log.error("Nothing to send for user: {}", user.getUsername());
            throw new RuntimeException("Email change code not found");
        }

        Codes code = optionalCode.get();
        Context context = new Context();
        context.setVariable("emailcode", code.getCode());
        context.setVariable("expiration", code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        context.setVariable("year", LocalDate.now().getYear());

        String htmlMessage = templateEngine.process("email-change", context);

        String subject = "Email change code";
        try {
            emailService.sendEmail(email, subject, htmlMessage);
            log.info("Email change code sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send email change code to: {}", user.getEmail(), e);
        }
    }

    public void resetEmail(Long userId, String emailCode, String email) {
        log.info("Changing email for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("User not found");
                });

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.EMAIL_CHANGE);
        if (optionalCode.isPresent()) {
            Codes code = optionalCode.get();

            if (code.getExpiration().isBefore(LocalDateTime.now())) {
                log.error("Email change code has expired for userId: {}", userId);
                throw new RuntimeException("Email change code has expired");
            }

            if (!code.getCode().equals(emailCode)) {
                log.error("Invalid email change code for userId: {}. Provided: {}", userId, emailCode);
                throw new RuntimeException("Invalid email change code");
            }

            user.setEmail(email);
            codeRepository.delete(code);
            userRepository.save(user);
            log.info("Email changed successful for userId: {}", userId);
        }
    }
}
