package com.altester.core.dtos.auth_service.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {
  private String name;
  private String surname;
  private String email;
  private String password;
}
