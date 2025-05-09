package com.altester.auth.dto.Auth;

import lombok.Data;

@Data
public class LoginResponse {
  private String token;
  private String userRole;
  private String message;

  public LoginResponse(String token, String userRole, String message) {
    this.token = token;
    this.userRole = userRole;
    this.message = message;
  }
}
