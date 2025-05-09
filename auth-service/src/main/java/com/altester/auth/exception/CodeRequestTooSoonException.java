package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class CodeRequestTooSoonException extends AuthException {
  private final String codeType;
  private final String email;

  public CodeRequestTooSoonException(String codeType, String email) {
    super(
        codeType + " code was requested less than a minute ago for: " + email,
        determineErrorCode(codeType));
    this.codeType = codeType;
    this.email = email;
  }

  private static AuthErrorCode determineErrorCode(String codeType) {
    if (codeType.equals("Verification")) {
      return AuthErrorCode.VERIFICATION_TOO_SOON;
    } else if (codeType.equals("Password reset")) {
      return AuthErrorCode.PASSWORD_RESET_TOO_SOON;
    } else {
      return AuthErrorCode.EMAIL_CHANGE_TOO_SOON;
    }
  }
}
