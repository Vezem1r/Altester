package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class LdapAuthException extends AuthException {
    private final String username;
    private final String reason;

    public LdapAuthException(String username, String reason) {
        super("LDAP authentication failed for user: " + username + ". Reason: " + reason,
                AuthErrorCode.LDAP_AUTH_FAILED);
        this.username = username;
        this.reason = reason;
    }
}