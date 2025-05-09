package com.altester.core.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends AlTesterException {
  private final String resourceType;
  private final String identifier;

  public ResourceAlreadyExistsException(
      String resourceType, String identifier, String customMessage) {
    super(
        customMessage != null
            ? customMessage
            : resourceType + " with identifier '" + identifier + "' already exists",
        ErrorCode.RESOURCE_ALREADY_EXISTS);
    this.resourceType = resourceType;
    this.identifier = identifier;
  }

  public static ResourceAlreadyExistsException user(String username) {
    return new ResourceAlreadyExistsException("User", username, null);
  }

  public static ResourceAlreadyExistsException group(String name) {
    return new ResourceAlreadyExistsException("Group", name, null);
  }

  public static ResourceAlreadyExistsException subject(String shortName) {
    return new ResourceAlreadyExistsException("Subject", shortName, null);
  }
}
