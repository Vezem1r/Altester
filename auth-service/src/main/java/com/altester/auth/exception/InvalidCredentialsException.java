package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends AuthException {
  public InvalidCredentialsException() {
    super("Invalid credentials provided", AuthErrorCode.INVALID_CREDENTIALS);
  }
}
