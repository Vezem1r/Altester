package com.altester.auth.dto;

import lombok.Data;

@Data
public class EmailInitDTO {
    private String email;
    private String password;
    private String username;
}
