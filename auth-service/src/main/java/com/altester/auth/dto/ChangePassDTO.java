package com.altester.auth.dto;

import lombok.Data;

@Data
public class ChangePassDTO {
    private String resetCode;
    private String newPassword;
}
