package com.altester.core.dtos.auth_service.pass;

import lombok.Data;

@Data
public class ChangePassDTO {
    private Long userId;
    private String resetCode;
    private String newPassword;
}
