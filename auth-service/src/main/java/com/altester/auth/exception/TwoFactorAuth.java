package com.altester.auth.exception;

public class TwoFactorAuth extends RuntimeException {
    public TwoFactorAuth(String message) {
        super(message);
    }
}
