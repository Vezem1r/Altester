package com.altester.auth.dto;

import lombok.Data;

@Data
public class TwoFaDTO {
        private String emailOrUsername;
        private String twoFactorCode;
}
