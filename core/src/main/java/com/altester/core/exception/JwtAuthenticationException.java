package com.altester.core.exception;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {

    private final String errorCode;

    public JwtAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static JwtAuthenticationException invalidToken() {
        return new JwtAuthenticationException("Invalid authentication token", "AUTH-601");
    }

    public static JwtAuthenticationException expiredToken() {
        return new JwtAuthenticationException("Authentication token has expired", "AUTH-602");
    }

    public static JwtAuthenticationException malformedToken() {
        return new JwtAuthenticationException("Malformed authentication token", "AUTH-603");
    }
}