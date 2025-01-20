package com.altester.auth.dto;

import lombok.Data;

@Data
public class VerifyUserDTO {
    private String email;
    private String verificationCode;
}
