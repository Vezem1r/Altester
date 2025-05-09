package com.altester.core.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AlTesterException.class)
  public ResponseEntity<ErrorResponse> handleAlTesterException(AlTesterException ex) {
    log.error("Application exception: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = ErrorResponse.from(ex);
    return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    log.error("Constraint violation: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), "Validation failed");

    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String propertyPath = violation.getPropertyPath().toString();
              String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
              errorResponse.addDetail(field, violation.getMessage());
            });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    log.error("Method argument not valid: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), "Validation failed");

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    errorResponse.addDetail("validationErrors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
    log.error("Binding exception: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), "Binding error");

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    errorResponse.addDetail("bindingErrors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    log.error("Type mismatch: {}", ex.getMessage(), ex);

    String paramName = ex.getName();
    String requiredType =
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
    String providedValue = ex.getValue() != null ? ex.getValue().toString() : "null";

    if ("semester".equals(paramName) && "Semester".equals(requiredType)) {
      ErrorResponse errorResponse =
          new ErrorResponse(
              ErrorCode.VALIDATION_ERROR.getCode(),
              "Invalid semester value. Valid values are: WINTER, SUMMER");

      errorResponse.addDetail("parameter", paramName);
      errorResponse.addDetail("validValues", "WINTER, SUMMER");
      errorResponse.addDetail("providedValue", providedValue);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    ErrorResponse errorResponse =
        new ErrorResponse(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "Parameter '" + paramName + "' should be of type '" + requiredType + "'");

    errorResponse.addDetail("parameter", paramName);
    errorResponse.addDetail("requiredType", requiredType);
    errorResponse.addDetail("providedValue", providedValue);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "An unexpected error occurred. Please try again later.");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
