package com.altester.ai_grading_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class AiApiServiceException extends Exception {
  private final HttpStatusCode httpStatus;
  private final String responseBody;

  public AiApiServiceException(String message, HttpStatusCode httpStatus) {
    this(message, httpStatus, null);
  }

  public AiApiServiceException(String message, HttpStatusCode httpStatus, String responseBody) {
    super(message);
    this.httpStatus = httpStatus;
    this.responseBody = responseBody;
  }

  public AiApiServiceException(String message, Throwable cause) {
    super(message, cause);
    this.httpStatus = null;
    this.responseBody = null;
  }
}