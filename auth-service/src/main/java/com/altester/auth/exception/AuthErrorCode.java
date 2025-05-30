package com.altester.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {
  INVALID_CREDENTIALS("AUTH-100", HttpStatus.UNAUTHORIZED),
  USER_DISABLED("AUTH-102", HttpStatus.FORBIDDEN),

  EMAIL_ALREADY_EXISTS("AUTH-200", HttpStatus.CONFLICT),
  EMAIL_CHANGE_TOO_SOON("AUTH-204", HttpStatus.TOO_MANY_REQUESTS),

  PASSWORD_RESET_CODE_NOT_FOUND("AUTH-300", HttpStatus.NOT_FOUND),
  PASSWORD_RESET_CODE_EXPIRED("AUTH-301", HttpStatus.GONE),
  PASSWORD_RESET_CODE_INVALID("AUTH-302", HttpStatus.BAD_REQUEST),
  PASSWORD_RESET_TOO_SOON("AUTH-303", HttpStatus.TOO_MANY_REQUESTS),

  VERIFICATION_CODE_NOT_FOUND("AUTH-401", HttpStatus.NOT_FOUND),
  VERIFICATION_CODE_EXPIRED("AUTH-402", HttpStatus.GONE),
  VERIFICATION_CODE_INVALID("AUTH-403", HttpStatus.BAD_REQUEST),
  VERIFICATION_TOO_SOON("AUTH-404", HttpStatus.TOO_MANY_REQUESTS),

  LDAP_AUTH_FAILED("AUTH-500", HttpStatus.UNAUTHORIZED),
  LDAP_USER_OPERATION("AUTH-501", HttpStatus.FORBIDDEN),

  INVALID_REQUEST("AUTH-900", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("AUTH-999", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final HttpStatus httpStatus;

  AuthErrorCode(String code, HttpStatus httpStatus) {
    this.code = code;
    this.httpStatus = httpStatus;
  }
}
