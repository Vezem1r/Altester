package com.altester.core.dtos.auth_service.email;

import lombok.Data;

@Data
public class EmailResendDTO {
    private String email;
    private String username;
}
