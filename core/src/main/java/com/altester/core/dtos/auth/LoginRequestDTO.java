package com.altester.core.dtos.auth;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String usernameOrEmail;
    private String password;
    private boolean rememberMe;
}
