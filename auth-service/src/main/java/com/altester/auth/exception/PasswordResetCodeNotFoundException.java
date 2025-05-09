package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class PasswordResetCodeNotFoundException extends AuthException {
  private final String email;

  public PasswordResetCodeNotFoundException(String email) {
    super("Password reset code not found", AuthErrorCode.PASSWORD_RESET_CODE_NOT_FOUND);
    this.email = email;
  }
}
