package com.altester.core.dtos.auth_service.auth;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String role;
    private String password;
    private String username;
}
