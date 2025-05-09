package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class UserDisabledException extends AuthException {
  private final String username;

  public UserDisabledException(String username) {
    super("User account is disabled: " + username, AuthErrorCode.USER_DISABLED);
    this.username = username;
  }
}
