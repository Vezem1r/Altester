package com.altester.core.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {

  private static final String KEY_VALID = "valid";
  private static final String KEY_ERROR_CODE = "errorCode";
  private static final String KEY_MESSAGE = "message";

  @ExceptionHandler(JwtAuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleJwtAuthenticationException(
      JwtAuthenticationException ex) {
    log.warn("JWT authentication error: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(KEY_VALID, false);
    response.put(KEY_ERROR_CODE, ex.getErrorCode());
    response.put(KEY_MESSAGE, ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex) {
    log.warn("Token expired: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(KEY_VALID, false);
    response.put("expired", true);
    response.put(KEY_ERROR_CODE, "AUTH-602");
    response.put(KEY_MESSAGE, "Authentication token has expired");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
  public ResponseEntity<Map<String, Object>> handleInvalidTokenException(Exception ex) {
    log.warn("Invalid token signature or format: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(KEY_VALID, false);
    response.put(KEY_ERROR_CODE, "AUTH-601");
    response.put(KEY_MESSAGE, "Invalid authentication token");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<Map<String, Object>> handleGeneralJwtException(JwtException ex) {
    log.warn("JWT exception: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(KEY_VALID, false);
    response.put(KEY_ERROR_CODE, "AUTH-603");
    response.put(KEY_MESSAGE, "Malformed authentication token");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex) {
    log.warn("Authentication error: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(KEY_VALID, false);
    response.put(KEY_ERROR_CODE, "AUTH-100");
    response.put(KEY_MESSAGE, "Authentication failed");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
}
