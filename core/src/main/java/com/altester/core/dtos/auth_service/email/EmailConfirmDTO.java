package com.altester.core.dtos.auth_service.email;

import lombok.Data;

@Data
public class EmailConfirmDTO {
  private Long userId;
  private String email;
  private String emailCode;
}
