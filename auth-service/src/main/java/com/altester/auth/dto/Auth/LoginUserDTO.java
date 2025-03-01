package com.altester.auth.dto.Auth;

import lombok.Data;

@Data
public class LoginUserDTO {
    private String usernameOrEmail;
    private String password;
    private boolean rememberMe;
}
