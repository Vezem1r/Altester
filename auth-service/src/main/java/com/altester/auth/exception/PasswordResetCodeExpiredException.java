package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class PasswordResetCodeExpiredException extends AuthException {
  private final String email;

  public PasswordResetCodeExpiredException(String email) {
    super(
        "Password reset code has expired for email: " + email,
        AuthErrorCode.PASSWORD_RESET_CODE_EXPIRED);
    this.email = email;
  }
}
