package com.altester.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class ErrorResponse {
  private final String errorCode;
  private final String message;
  private final LocalDateTime timestamp;
  private final Map<String, Object> details;

  public ErrorResponse(String errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
    this.timestamp = LocalDateTime.now();
    this.details = new HashMap<>();
  }

  public static ErrorResponse from(AlTesterException exception) {
    ErrorResponse response =
        new ErrorResponse(exception.getErrorCode().getCode(), exception.getMessage());

    switch (exception) {
      case ResourceNotFoundException resourceNotFoundException -> {
        response.addDetail("resourceType", resourceNotFoundException.getResourceType());
        response.addDetail("identifier", resourceNotFoundException.getIdentifier());
      }
      case ResourceAlreadyExistsException resourceAlreadyExistsException -> {
        response.addDetail("resourceType", resourceAlreadyExistsException.getResourceType());
        response.addDetail("identifier", resourceAlreadyExistsException.getIdentifier());
      }
      case ValidationException validationException -> {
        response.addDetail("field", validationException.getField());
        response.addDetail("violation", validationException.getViolation());
      }
      case AccessDeniedException accessDeniedException -> {
        response.addDetail("resource", accessDeniedException.getResource());
        response.addDetail("action", accessDeniedException.getAction());
      }
      case StateConflictException stateConflictException -> {
        response.addDetail("resource", stateConflictException.getResource());
        response.addDetail("currentState", stateConflictException.getCurrentState());
      }
      case FileOperationException fileOperationException ->
          response.addDetail("operation", fileOperationException.getOperation());
      default -> log.warn("Unhandled exception type: {}", exception.getClass().getName());
    }

    return response;
  }

  public void addDetail(String key, Object value) {
    if (value != null) {
      this.details.put(key, value);
    }
  }
}
