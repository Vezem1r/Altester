package com.altester.auth.dto;

import lombok.Data;

@Data
public class EmailConfirmDTO {
    private String email;
    private String emailCode;
}
