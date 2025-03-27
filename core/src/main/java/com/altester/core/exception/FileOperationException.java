package com.altester.core.exception;

import lombok.Getter;

@Getter
public class FileOperationException extends AlTesterException {
    private final String operation;

    public FileOperationException(String operation, String message) {
        super(message, ErrorCode.FILE_OPERATION_ERROR);
        this.operation = operation;
    }

    public static FileOperationException imageSave(String message) {
        return new FileOperationException("save", message);
    }
}
