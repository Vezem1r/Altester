package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class VerificationCodeNotFoundException extends AuthException {
    private final String email;

    public VerificationCodeNotFoundException(String email) {
        super("Verification code not found", AuthErrorCode.VERIFICATION_CODE_NOT_FOUND);
        this.email = email;
    }
}