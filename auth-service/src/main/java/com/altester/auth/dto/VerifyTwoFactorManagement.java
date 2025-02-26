package com.altester.auth.dto;

import lombok.Data;

@Data
public class VerifyTwoFactorManagement {
    private Long userId;
    private String code;
}
