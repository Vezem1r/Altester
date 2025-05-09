package com.altester.auth.service.serviceImpl;

import com.altester.auth.dto.EmailConfirmDTO;
import com.altester.auth.dto.EmailInitDTO;
import com.altester.auth.dto.EmailResendDTO;
import com.altester.auth.exception.*;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.UserEmailService;
import com.altester.auth.utils.Constants;
import com.altester.auth.utils.EmailUtils;
import com.altester.auth.utils.UserUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailServiceImpl implements UserEmailService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CodeRepository codeRepository;
  private final EmailUtils emailUtils;
  private final UserUtils userUtils;

  @Override
  public void initiateEmailReset(EmailInitDTO emailInitDTO) {
    log.info("Attempting to initiate email reset for user: {}", emailInitDTO.getUsername());
    User user = findUserByUsername(emailInitDTO.getUsername());

    if (user.getRole().equals(RolesEnum.ADMIN)) {
      log.error("Admin user cannot reset email: {}", user.getUsername());
      throw new LdapUserOperationException(user.getUsername(), "email reset");
    }

    if (!user.isRegistered()) {
      log.error("User was created using LDAP: {}", user.getUsername());
      throw new LdapUserOperationException(user.getUsername(), "email reset");
    }

    if (!passwordEncoder.matches(emailInitDTO.getPassword(), user.getPassword())) {
      log.error("Invalid password for user: {}", user.getUsername());
      throw new InvalidCredentialsException();
    }

    removeExistingCode(user);

    String resetCode = userUtils.generateVerificationCode();
    Codes code = createEmailChangeCode(user, resetCode);
    codeRepository.save(code);

    emailUtils.sendVerificationEmail(user, EmailType.CHANGE_EMAIL, emailInitDTO.getEmail());
    log.info("Email reset initiated for user: {}", emailInitDTO.getUsername());
  }

  @Override
  public void resendMailCode(EmailResendDTO emailResendDTO) {
    log.info(
        "Attempting to resend email verification code for user: {}", emailResendDTO.getUsername());
    User user = findUserByUsername(emailResendDTO.getUsername());
    Codes code = findEmailChangeCode(user);

    validateCodeResendTime(code, emailResendDTO.getEmail());

    code.setCode(userUtils.generateVerificationCode());
    code.setExpiration(LocalDateTime.now().plusMinutes(Constants.USER_CODE_TTL));
    code.setSendAt(LocalDateTime.now());
    codeRepository.save(code);

    emailUtils.sendVerificationEmail(user, EmailType.CHANGE_EMAIL, emailResendDTO.getEmail());
    log.info("Email verification code resent for user: {}", emailResendDTO.getUsername());
  }

  @Override
  public void resetEmail(EmailConfirmDTO emailConfirmDTO) {
    User user = findUserById(emailConfirmDTO.getUserId());
    Codes code = findEmailChangeCode(user);

    if (code.getExpiration().isBefore(LocalDateTime.now())) {
      log.error("Email change code has expired for user: {}", user.getEmail());
      throw new VerificationCodeExpiredException(user.getEmail());
    }

    if (!code.getCode().equals(emailConfirmDTO.getEmailCode())) {
      log.error("Invalid email change code for user: {}", user.getEmail());
      throw new InvalidVerificationCodeException(user.getEmail());
    }

    user.setEmail(emailConfirmDTO.getEmail());
    codeRepository.delete(code);
    userRepository.save(user);
    log.info("Email reset confirmed for user: {}", user.getUsername());
  }

  private User findUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> {
              log.error("User not found with username: {}", username);
              return new UserNotFoundException(username);
            });
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.error("User not found with ID: {}", userId);
              return new UserNotFoundException(userId.toString());
            });
  }

  private Codes findEmailChangeCode(User user) {
    return codeRepository
        .findByUserAndCodeType(user, CodeType.EMAIL_CHANGE)
        .orElseThrow(
            () -> {
              log.error("Email change code not found for user: {}", user.getEmail());
              return new VerificationCodeNotFoundException(user.getEmail());
            });
  }

  private void validateCodeResendTime(Codes code, String email) {
    if (code.getSendAt()
        .plusSeconds(Constants.USER_CODE_RESEND_TIMEOUT)
        .isAfter(LocalDateTime.now())) {
      log.error("Email change code was sent less than a minute ago for user: {}", email);
      throw new CodeRequestTooSoonException("Email change", email);
    }
  }

  private void removeExistingCode(User user) {
    codeRepository
        .findByUserAndCodeType(user, CodeType.EMAIL_CHANGE)
        .ifPresent(codeRepository::delete);
  }

  private Codes createEmailChangeCode(User user, String code) {
    Codes emailCode = new Codes();
    emailCode.setSendAt(LocalDateTime.now());
    emailCode.setUser(user);
    emailCode.setCodeType(CodeType.EMAIL_CHANGE);
    emailCode.setCode(code);
    emailCode.setExpiration(LocalDateTime.now().plusMinutes(Constants.USER_CODE_TTL));
    return emailCode;
  }
}
