package com.altester.auth.dto;

import lombok.Data;

@Data
public class LoginUserDTO {
    private String usernameOrEmail;
    private String password;
}
