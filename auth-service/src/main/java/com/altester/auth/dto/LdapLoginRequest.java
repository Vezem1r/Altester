package com.altester.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LdapLoginRequest {
  @NotBlank(message = "Login cannot be empty")
  private String login;

  @NotBlank(message = "Password cannot be empty")
  private String password;
}
