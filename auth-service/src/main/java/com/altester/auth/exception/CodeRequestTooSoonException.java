package com.altester.auth.exception;

import lombok.Getter;

@Getter
public class CodeRequestTooSoonException extends AuthException {
    private final String codeType;
    private final String email;

    public CodeRequestTooSoonException(String codeType, String email) {
        super(codeType + " code was requested less than a minute ago for: " + email,
                codeType.equals("Verification") ? AuthErrorCode.VERIFICATION_TOO_SOON :
                        codeType.equals("Password reset") ? AuthErrorCode.PASSWORD_RESET_TOO_SOON :
                                AuthErrorCode.EMAIL_CHANGE_TOO_SOON);
        this.codeType = codeType;
        this.email = email;
    }
}
