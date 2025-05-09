package com.altester.chat_service.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

  public ResourceNotFoundException(String resourceType, String identifier) {
    super(
        resourceType + " with identifier '" + identifier + "' not found",
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND");
  }
}
