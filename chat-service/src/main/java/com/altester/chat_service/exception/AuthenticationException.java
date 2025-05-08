package com.altester.chat_service.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException{

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}