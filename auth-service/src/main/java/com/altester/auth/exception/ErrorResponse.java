package com.altester.auth.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;
    private Map<String, Object> details;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse from(AuthException exception) {
        String sanitizedMessage = getSanitizedMessage(exception);

        ErrorResponse response = new ErrorResponse(
                exception.getErrorCode().getCode(),
                sanitizedMessage
        );

        if (shouldAddDetails(exception)) {
            response.details = new HashMap<>();
            addSafeDetails(response, exception);
        }

        return response;
    }

    private static String getSanitizedMessage(AuthException exception) {
        return switch (exception) {
            case UserNotFoundException ignored -> "Invalid credentials";
            case InvalidCredentialsException ignored -> "Invalid credentials";
            case LdapAuthException ignored1 -> "Authentication failed";

            case EmailAlreadyExistsException ignored -> "This email is already registered";
            case VerificationCodeExpiredException ignored1 -> "The verification code has expired. Please request a new one";
            case PasswordResetCodeExpiredException ignored -> "The verification code has expired. Please request a new one";
            case InvalidVerificationCodeException ignored -> "Invalid verification code";
            case InvalidPasswordResetCodeException ignored -> "Invalid verification code";

            case CodeRequestTooSoonException ignored -> "Please wait before requesting another code";

            default -> exception.getMessage();
        };
    }

    private static boolean shouldAddDetails(AuthException exception) {
        return !(exception instanceof UserNotFoundException ||
                exception instanceof InvalidCredentialsException ||
                exception instanceof LdapAuthException);
    }

    private static void addSafeDetails(ErrorResponse response, AuthException exception) {
        if (Objects.requireNonNull(exception) instanceof CodeRequestTooSoonException tooSoon) {
            response.addDetail("codeType", tooSoon.getCodeType());
        }
    }

    public void addDetail(String key, Object value) {
        if (value != null) {
            if (this.details == null) {
                this.details = new HashMap<>();
            }
            this.details.put(key, value);
        }
    }
}