package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class LdapUserOperationException extends AuthException {
    private final String username;
    private final String operation;

    public LdapUserOperationException(String username, String operation) {
        super("Cannot perform " + operation + " for LDAP user: " + username, AuthErrorCode.LDAP_USER_OPERATION);
        this.username = username;
        this.operation = operation;
    }
}
