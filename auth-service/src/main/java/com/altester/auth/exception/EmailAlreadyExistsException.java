package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends AuthException {
  private final String email;

  public EmailAlreadyExistsException(String email) {
    super("Email already exists: " + email, AuthErrorCode.EMAIL_ALREADY_EXISTS);
    this.email = email;
  }
}
