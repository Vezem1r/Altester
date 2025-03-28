package com.altester.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AuthException extends RuntimeException {

    private final AuthErrorCode errorCode;

    protected AuthException(String message, AuthErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}