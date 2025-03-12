package com.altester.auth.dto;

import lombok.Data;

@Data
public class ChangePassDTO {
    private String email;
    private String verificationCode;
    private String newPassword;
}
