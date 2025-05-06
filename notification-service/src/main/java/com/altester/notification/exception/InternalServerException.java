package com.altester.notification.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException{

    public InternalServerException(String message, Throwable cause) {
        super(message != null ? message : "An internal server error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                cause);
    }
}
