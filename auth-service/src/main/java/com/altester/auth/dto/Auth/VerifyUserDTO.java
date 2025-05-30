package com.altester.auth.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyUserDTO {
  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Verification code is required")
  @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
  private String verificationCode;
}
