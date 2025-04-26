package com.altester.core.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends AlTesterException {
    private final String resource;
    private final String action;

    public AccessDeniedException(String resource, String action, String customMessage) {
        super(customMessage != null ? customMessage :
                        "Access denied: Cannot " + action + " " + resource,
                ErrorCode.ACCESS_DENIED);
        this.resource = resource;
        this.action = action;
    }

    public static AccessDeniedException apiKeyAccess(String message) {
        return new AccessDeniedException("apiKey", "access", message);
    }

    public static AccessDeniedException testAccess() {
        return new AccessDeniedException("test", "access", null);
    }

    public static AccessDeniedException testEdit() {
        return new AccessDeniedException("test", "edit", null);
    }

    public static AccessDeniedException groupAccess() {
        return new AccessDeniedException("group", "access", null);
    }

    public static AccessDeniedException attemptAccess() {
        return new AccessDeniedException("attempt", "access", null);
    }

    public static AccessDeniedException notAdmin() {
        return new AccessDeniedException("admin-only resource", "access", "Only administrators can perform this action");
    }

    public static AccessDeniedException ldapUserModification() {
        return new AccessDeniedException("LDAP user", "modify", "LDAP users cannot be modified directly");
    }

    public static AccessDeniedException roleConflict() {
        return new AccessDeniedException("role-protected resource", "access", "User does not have the required role to perform this action");
    }
}