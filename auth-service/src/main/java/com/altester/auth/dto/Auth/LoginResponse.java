package com.altester.auth.dto.Auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private long expiredIn;
    private String message;

    public LoginResponse(String token, long expiredIn, String message) {
        this.token = token;
        this.expiredIn = expiredIn;
        this.message = message;
    }
}
