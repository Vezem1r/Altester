package com.altester.auth.utils;

import com.altester.auth.exception.PasswordResetCodeNotFoundException;
import com.altester.auth.exception.VerificationCodeNotFoundException;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.service.EmailService;
import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {

  private final CodeRepository codeRepository;
  private final EmailService emailService;
  private final TemplateEngine templateEngine;

  public void sendVerificationEmail(User user, EmailType emailType) {
    // Register
    if (emailType.equals(EmailType.REGISTER)) {
      Optional<Codes> optionalCode =
          codeRepository.findByUserAndCodeType(user, CodeType.VERIFICATION);
      if (optionalCode.isEmpty()) {
        log.error("User verification code does not exists: {}", user.getUsername());
        throw new VerificationCodeNotFoundException("Verification code not found");
      }

      Codes code = optionalCode.get();
      Context context = new Context();
      context.setVariable("verificationCode", code.getCode());
      context.setVariable(
          "expiration",
          code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
      context.setVariable("year", LocalDate.now().getYear());

      String htmlMessage = templateEngine.process("verification-email", context);
      String subject = "Account Verification";
      sendEmail(user.getEmail(), subject, htmlMessage);
    }

    // Change pass
    if (emailType.equals(EmailType.CHANGE_PASS)) {
      Optional<Codes> optionalCode =
          codeRepository.findByUserAndCodeType(user, CodeType.PASSWORD_RESET);
      if (optionalCode.isEmpty()) {
        log.error("Password reset code does not exists`: {}", user.getUsername());
        throw new PasswordResetCodeNotFoundException("Password reset code not found");
      }

      Codes code = optionalCode.get();
      Context context = new Context();
      context.setVariable("resetCode", code.getCode());
      context.setVariable(
          "expiration",
          code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
      context.setVariable("year", LocalDate.now().getYear());

      String htmlMessage = templateEngine.process("password-reset-email", context);
      String subject = "Password reset code";
      sendEmail(user.getEmail(), subject, htmlMessage);
    }
  }

  // Email reset
  public void sendVerificationEmail(User user, EmailType emailType, String email) {

    if (emailType.equals(EmailType.CHANGE_EMAIL)) {
      Optional<Codes> optionalCode =
          codeRepository.findByUserAndCodeType(user, CodeType.EMAIL_CHANGE);
      if (optionalCode.isEmpty()) {
        log.error("Email reset code does not exists: {}", user.getUsername());
        throw new VerificationCodeNotFoundException("Email change code not found");
      }

      Codes code = optionalCode.get();
      Context context = new Context();
      context.setVariable("emailcode", code.getCode());
      context.setVariable(
          "expiration",
          code.getExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
      context.setVariable("year", LocalDate.now().getYear());

      String htmlMessage = templateEngine.process("email-change", context);

      String subject = "Email change code";

      sendEmail(email, subject, htmlMessage);
    } else {
      throw new RuntimeException("Function expects email type to be 'CHANGE_EMAIL'");
    }
  }

  private void sendEmail(String email, String subject, String htmlMessage) {
    try {
      emailService.sendEmail(email, subject, htmlMessage);
      log.info("Verification email sent to: {}", email);
    } catch (MessagingException e) {
      log.error("Failed to send verification email to: {}", email, e);
    }
  }
}
