package com.altester.auth.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUserDTO {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
    private boolean rememberMe;
}
