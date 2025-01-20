package com.altester.auth.dto;

import lombok.Data;

@Data
public class RegisterUserDTO {
    private String name;
    private String surname;
    private String email;
    private String password;
}
