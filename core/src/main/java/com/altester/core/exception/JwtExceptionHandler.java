package com.altester.core.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<?> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        log.warn("JWT authentication error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("Token expired: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("expired", true);
        response.put("errorCode", "AUTH-602");
        response.put("message", "Authentication token has expired");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<?> handleInvalidTokenException(Exception ex) {
        log.warn("Invalid token signature or format: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("errorCode", "AUTH-601");
        response.put("message", "Invalid authentication token");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleGeneralJwtException(JwtException ex) {
        log.warn("JWT exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("errorCode", "AUTH-603");
        response.put("message", "Malformed authentication token");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleAuthenticationException(Exception ex) {
        log.warn("Authentication error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("errorCode", "AUTH-100");
        response.put("message", "Authentication failed");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}