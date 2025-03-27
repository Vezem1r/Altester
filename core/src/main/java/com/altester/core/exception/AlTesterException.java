package com.altester.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AlTesterException extends RuntimeException {
    private final ErrorCode errorCode;

    protected AlTesterException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}

