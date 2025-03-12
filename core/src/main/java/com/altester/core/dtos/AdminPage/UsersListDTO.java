package com.altester.core.dtos.AdminPage;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsersListDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private LocalDateTime lastLogin;
}
