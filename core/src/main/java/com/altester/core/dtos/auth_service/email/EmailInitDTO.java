package com.altester.core.dtos.auth_service.email;

import lombok.Data;

@Data
public class EmailInitDTO {
    private String email;
    private String password;
    private String username;
}
