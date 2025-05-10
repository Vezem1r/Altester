package com.altester.core.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends AlTesterException {
  private final String resource;
  private final String action;

  private static final String ACTION_ACCESS = "access";

  public AccessDeniedException(String resource, String action, String customMessage) {
    super(
        customMessage != null ? customMessage : "Access denied: Cannot " + action + " " + resource,
        ErrorCode.ACCESS_DENIED);
    this.resource = resource;
    this.action = action;
  }

  public static AccessDeniedException apiKeyAccess(String message) {
    return new AccessDeniedException("apiKey", ACTION_ACCESS, message);
  }

  public static AccessDeniedException promptAccess(String message) {
    return new AccessDeniedException("prompt", ACTION_ACCESS, message);
  }

  public static AccessDeniedException testAccess() {
    return new AccessDeniedException("test", ACTION_ACCESS, null);
  }

  public static AccessDeniedException testEdit() {
    return new AccessDeniedException("test", "edit", null);
  }

  public static AccessDeniedException groupAccess() {
    return new AccessDeniedException("group", ACTION_ACCESS, null);
  }

  public static AccessDeniedException attemptAccess() {
    return new AccessDeniedException("attempt", ACTION_ACCESS, null);
  }

  public static AccessDeniedException notAdmin() {
    return new AccessDeniedException(
        "admin-only resource", ACTION_ACCESS, "Only administrators can perform this action");
  }

  public static AccessDeniedException ldapUserModification() {
    return new AccessDeniedException(
        "LDAP user", "modify", "LDAP users cannot be modified directly");
  }

  public static AccessDeniedException roleConflict() {
    return new AccessDeniedException(
        "role-protected resource",
        ACTION_ACCESS,
        "User does not have the required role to perform this action");
  }
}
