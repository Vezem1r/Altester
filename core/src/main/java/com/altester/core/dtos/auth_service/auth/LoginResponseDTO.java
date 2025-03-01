package com.altester.core.dtos.auth_service.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private long expiredIn;
    private String message;
}
