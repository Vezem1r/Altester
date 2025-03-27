package com.altester.core.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("Token expired: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("expired", true);
        response.put("message", "Authentication token has expired");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtException(JwtException ex) {
        log.warn("Invalid token: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("message", "Invalid authentication token");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}