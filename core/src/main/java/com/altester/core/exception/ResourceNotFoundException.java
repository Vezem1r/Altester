package com.altester.core.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends AlTesterException {
    private final String resourceType;
    private final String identifier;

    public ResourceNotFoundException(String resourceType, String identifier, String customMessage) {
        super(customMessage != null ? customMessage :
                        resourceType + " with identifier '" + identifier + "' not found",
                ErrorCode.RESOURCE_NOT_FOUND);
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public static ResourceNotFoundException apiKey(Long id) {
        return new ResourceNotFoundException("ApiKey", id.toString(), null);
    }

    public static ResourceNotFoundException user(String identifier) {
        return new ResourceNotFoundException("User", identifier, null);
    }

    public static ResourceNotFoundException user(String identifier, String message) {
        return new ResourceNotFoundException("User", identifier, message);
    }

    public static ResourceNotFoundException user(Long id) {
        return user(id.toString());
    }

    public static ResourceNotFoundException group(Long id) {
        return new ResourceNotFoundException("Group", id.toString(), null);
    }

    public static ResourceNotFoundException group(String message) {
        return new ResourceNotFoundException(null, null, message);
    }

    public static ResourceNotFoundException subject(Long id) {
        return new ResourceNotFoundException("Subject", id.toString(), null);
    }

    public static ResourceNotFoundException test(Long id) {
        return new ResourceNotFoundException("Test", id.toString(), null);
    }

    public static ResourceNotFoundException question(Long id) {
        return new ResourceNotFoundException("Question", id.toString(), null);
    }
}
