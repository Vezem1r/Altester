package com.altester.notification.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private final LocalDateTime timestamp;
  private final String status;
  private final String errorCode;
  private final String message;
  private final String path;
}
