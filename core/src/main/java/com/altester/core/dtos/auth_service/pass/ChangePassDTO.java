package com.altester.core.dtos.auth_service.pass;

import lombok.Data;

@Data
public class ChangePassDTO {
  private String email;
  private String verificationCode;
  private String newPassword;
}
