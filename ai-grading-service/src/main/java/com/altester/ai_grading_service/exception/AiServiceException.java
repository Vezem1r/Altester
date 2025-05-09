package com.altester.ai_grading_service.exception;

public class AiServiceException extends RuntimeException {

  public AiServiceException(String message) {
    super(message);
  }

  public AiServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public static AiServiceException invalidApiKey() {
    return new AiServiceException("Invalid or expired API key");
  }

  public static AiServiceException serviceUnavailable(String serviceName) {
    return new AiServiceException("AI service unavailable: " + serviceName);
  }

  public static AiServiceException gradingFailed(String reason) {
    return new AiServiceException("AI grading failed: " + reason);
  }
}
