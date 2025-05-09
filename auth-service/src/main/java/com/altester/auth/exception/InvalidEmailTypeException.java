package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class InvalidEmailTypeException extends AuthException {

  public InvalidEmailTypeException(String message) {
    super(message, AuthErrorCode.INVALID_REQUEST);
  }
}
