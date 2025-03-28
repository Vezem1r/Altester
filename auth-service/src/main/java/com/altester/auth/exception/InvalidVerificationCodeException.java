package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class InvalidVerificationCodeException extends AuthException {
    private final String email;

    public InvalidVerificationCodeException(String email) {
        super("Invalid verification code for email: " + email, AuthErrorCode.VERIFICATION_CODE_INVALID);
        this.email = email;
    }
}