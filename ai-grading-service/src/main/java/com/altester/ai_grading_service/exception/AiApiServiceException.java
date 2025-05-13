package com.altester.ai_grading_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class AiApiServiceException extends Exception {
  private final HttpStatusCode httpStatus;

  public AiApiServiceException(String message, HttpStatusCode httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
