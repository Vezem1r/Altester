package com.altester.core.exception;

import lombok.Getter;

@Getter
public class ValidationException extends AlTesterException {
  private final String field;
  private final String violation;

  public ValidationException(String message, String field, String violation) {
    super(message, ErrorCode.VALIDATION_ERROR);
    this.field = field;
    this.violation = violation;
  }

  public static ValidationException invalidQuestionType(String message) {
    return new ValidationException(message, "questionType", "INVALID_TYPE");
  }

  public static ValidationException invalidQuestionText(String message) {
    return new ValidationException(message, "questionText", "INVALID_TEXT");
  }

  public static ValidationException invalidImage(String message) {
    return new ValidationException(message, "image", "INVALID_IMAGE");
  }

  public static ValidationException invalidOption(String message) {
    return new ValidationException(message, "options", "INVALID_OPTION");
  }

  public static ValidationException invalidAiModel(String message) {
    return new ValidationException(message, "models", "INVALID_MODEL");
  }

  public static ValidationException missingCorrectOption(String message) {
    return new ValidationException(message, "options", "MISSING_CORRECT_OPTION");
  }

  public static ValidationException invalidPosition(String message) {
    return new ValidationException(message, "position", "INVALID_POSITION");
  }

  public static ValidationException groupValidation(String message) {
    return new ValidationException(message, "group", "INVALID_GROUP");
  }

  public static ValidationException invalidParameter(String parameterName, String message) {
    return new ValidationException(message, parameterName, "INVALID_PARAMETER");
  }
}
