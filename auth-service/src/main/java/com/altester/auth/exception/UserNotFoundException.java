package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends AuthException {
    private final String identifier;

    public UserNotFoundException(String identifier) {
        super("Invalid credentials", AuthErrorCode.INVALID_CREDENTIALS);
        this.identifier = identifier;
    }
}