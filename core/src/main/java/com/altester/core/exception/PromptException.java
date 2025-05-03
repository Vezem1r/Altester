package com.altester.core.exception;

import lombok.Getter;

@Getter
public class PromptException extends AlTesterException {

    public PromptException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public static PromptException promptNotFound(Long id) {
        return new PromptException(
                "Prompt not found",
                ErrorCode.RESOURCE_NOT_FOUND
        );
    }

    public static PromptException invalidPromptTemplate() {
        return new PromptException(
                "Prompt template is missing required placeholders",
                ErrorCode.VALIDATION_ERROR
        );
    }

    public static PromptException unauthorizedPromptAccess() {
        return new PromptException(
                "You don't have permission to access this prompt",
                ErrorCode.ACCESS_DENIED
        );
    }

    public static PromptException unauthorizedPromptModification() {
        return new PromptException(
                "You don't have permission to modify this prompt",
                ErrorCode.ACCESS_DENIED
        );
    }

    public static PromptException invalidPromptContent() {
        return new PromptException(
                "Prompt content contains invalid patterns",
                ErrorCode.VALIDATION_ERROR
        );
    }

    public static PromptException promptLimitExceeded(int maxCount) {
        return new PromptException(
                "Prompt limit exceeded. Maximum allowed prompts: " + maxCount,
                ErrorCode.VALIDATION_ERROR
        );
    }
}
