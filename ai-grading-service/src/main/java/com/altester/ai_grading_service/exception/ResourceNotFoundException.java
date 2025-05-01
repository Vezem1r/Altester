package com.altester.ai_grading_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public static ResourceNotFoundException attempt(Long id) {
        return new ResourceNotFoundException("Attempt", "id", id);
    }

    public static ResourceNotFoundException submission(Long id) {
        return new ResourceNotFoundException("Submission", "id", id);
    }

    public static ResourceNotFoundException question(Long id) {
        return new ResourceNotFoundException("Question", "id", id);
    }
}