package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class VerificationCodeExpiredException extends AuthException {
  private final String email;

  public VerificationCodeExpiredException(String email) {
    super(
        "Verification code has expired for email: " + email,
        AuthErrorCode.VERIFICATION_CODE_EXPIRED);
    this.email = email;
  }
}
