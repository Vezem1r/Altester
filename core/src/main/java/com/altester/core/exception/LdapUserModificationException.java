package com.altester.core.exception;

public class LdapUserModificationException extends RuntimeException {
    public LdapUserModificationException(String username) {
        super("User '" + username + "' was created via LDAP and cannot be modified");
    }
}