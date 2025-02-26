package com.altester.core.dtos.auth;

import lombok.Data;

@Data
public class VerifyUserDTO {
    private String email;
    private String verificationCode;
}
