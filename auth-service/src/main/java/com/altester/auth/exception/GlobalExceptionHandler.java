package com.altester.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        ErrorResponse errorResponse = ErrorResponse.from(ex);
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException() {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INVALID_CREDENTIALS.getCode(),
                "Invalid credentials"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INVALID_REQUEST.getCode(),
                "Invalid request data"
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        errorResponse.addDetail("validationErrors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();

        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INVALID_REQUEST.getCode(),
                "Invalid request format"
        );

        errorResponse.addDetail("parameter", paramName);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException() {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "An error occurred while processing your request"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException() {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INVALID_REQUEST.getCode(),
                "Invalid input data"
        );

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        });

        errorResponse.addDetail("validationErrors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                AuthErrorCode.INVALID_REQUEST.getCode(),
                "Invalid input data"
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        errorResponse.addDetail("validationErrors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}