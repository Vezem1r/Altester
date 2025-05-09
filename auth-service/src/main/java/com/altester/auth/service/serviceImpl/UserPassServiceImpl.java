package com.altester.auth.service.serviceImpl;

import com.altester.auth.dto.ChangePassDTO;
import com.altester.auth.exception.*;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.UserPassService;
import com.altester.auth.utils.Constants;
import com.altester.auth.utils.EmailUtils;
import com.altester.auth.utils.UserUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPassServiceImpl implements UserPassService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CodeRepository codeRepository;
  private final EmailUtils emailUtils;
  private final UserUtils userUtils;

  @Override
  public void initiatePasswordReset(String email) {
    log.info("Initiating password reset for {}", email);

    User user = findUserByEmailOrThrow(email);

    validateUserForPasswordReset(user);

    Optional<Codes> optionalCode =
        codeRepository.findByUserAndCodeType(user, CodeType.PASSWORD_RESET);
    if (optionalCode.isPresent()) {
      Codes code = optionalCode.get();

      validateCodeResendTime(code, email);

      log.info("Deleting previous password reset code for user: {}", user.getUsername());
      codeRepository.delete(code);
    }

    createAndSendPasswordResetCode(user);
  }

  @Override
  public void resetPassword(ChangePassDTO changePass) {
    log.info("Resetting password for user: {}", changePass.getEmail());

    User user = findUserByEmailOrThrow(changePass.getEmail());
    validateUserForPasswordReset(user);
    Codes code = findPasswordResetCodeOrThrow(user, changePass.getEmail());

    if (code.getExpiration().isBefore(LocalDateTime.now())) {
      log.error("Reset code has expired for user: {}", changePass.getEmail());
      throw new PasswordResetCodeExpiredException(changePass.getEmail());
    }

    if (!code.getCode().equals(changePass.getVerificationCode())) {
      log.error(
          "Invalid reset code for user: {}. Provided: {}",
          changePass.getEmail(),
          changePass.getVerificationCode());
      throw new InvalidPasswordResetCodeException(changePass.getEmail());
    }

    user.setPassword(passwordEncoder.encode(changePass.getNewPassword()));
    codeRepository.delete(code);
    userRepository.save(user);
    log.info("Password reset successful for user: {}", changePass.getEmail());
  }

  @Override
  public void resendResetCode(String email) {
    log.info("Attempting to resend password reset code for: {}", email);

    User user = findUserByEmailOrThrow(email);
    Codes code = findPasswordResetCodeOrThrow(user, email);
    validateCodeResendTime(code, email);

    code.setCode(userUtils.generateVerificationCode());
    code.setExpiration(LocalDateTime.now().plusMinutes(Constants.USER_CODE_TTL));
    code.setSendAt(LocalDateTime.now());
    codeRepository.save(code);

    emailUtils.sendVerificationEmail(user, EmailType.CHANGE_PASS);
    log.info("Resent password reset code to user: {}", email);
  }

  private void validateCodeResendTime(Codes code, String email) {
    if (code.getSendAt()
        .plusSeconds(Constants.USER_CODE_RESEND_TIMEOUT)
        .isAfter(LocalDateTime.now())) {
      log.warn("Password reset code was sent less than a minute ago for user: {}", email);
      throw new CodeRequestTooSoonException("Password reset", email);
    }
  }

  private User findUserByEmailOrThrow(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> {
              log.error("User not found with email: {}", email);
              return new UserNotFoundException(email);
            });
  }

  private Codes findPasswordResetCodeOrThrow(User user, String email) {
    return codeRepository
        .findByUserAndCodeType(user, CodeType.PASSWORD_RESET)
        .orElseThrow(
            () -> {
              log.error("Password reset code not found for user: {}", email);
              return new PasswordResetCodeNotFoundException(email);
            });
  }

  private void validateUserForPasswordReset(User user) {
    if (user.getRole().equals(RolesEnum.ADMIN)) {
      log.error("Admin user cannot reset password: {}", user.getUsername());
      throw new LdapUserOperationException(user.getUsername(), "password reset");
    }

    if (!user.isRegistered()) {
      log.error("User was created using LDAP: {}", user.getUsername());
      throw new LdapUserOperationException(user.getUsername(), "password reset");
    }
  }

  private void createAndSendPasswordResetCode(User user) {
    String resetCode = userUtils.generateVerificationCode();
    Codes code = new Codes();
    code.setSendAt(LocalDateTime.now());
    code.setUser(user);
    code.setCodeType(CodeType.PASSWORD_RESET);
    code.setCode(resetCode);
    code.setExpiration(LocalDateTime.now().plusMinutes(Constants.USER_CODE_TTL));

    codeRepository.save(code);
    emailUtils.sendVerificationEmail(user, EmailType.CHANGE_PASS);
    log.info("Password reset code sent to user: {}", user.getEmail());
  }
}
