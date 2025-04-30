package com.altester.core.exception;

import lombok.Getter;

@Getter
public class ApiKeyException extends AlTesterException {
    private final String operation;

    public ApiKeyException(String operation, String message) {
        super(message, ErrorCode.INTERNAL_SERVER_ERROR);
        this.operation = operation;
    }

    public static ApiKeyException encryptionError(String message) {
        return new ApiKeyException("encryption", message);
    }

    public static ApiKeyException decryptionError(String message) {
        return new ApiKeyException("encryption", message);
    }

    public static ApiKeyException invalidKeyLength() {
        return new ApiKeyException("validation", "Invalid AES key length. It must be 16, 24, or 32 bytes.");
    }

    public static ApiKeyException invalidInputKey(String message) {
        return new ApiKeyException("validation", message);
    }
}