package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class InvalidPasswordResetCodeException extends AuthException {
    private final String email;

    public InvalidPasswordResetCodeException(String email) {
        super("Invalid password reset code for email: " + email, AuthErrorCode.PASSWORD_RESET_CODE_INVALID);
        this.email = email;
    }
}