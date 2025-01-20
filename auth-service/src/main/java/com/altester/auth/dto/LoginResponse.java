package com.altester.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private long expiredIn;
    private String message;

    public LoginResponse(String token, String message) {
        this(token, 0, message);
    }
}