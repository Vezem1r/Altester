package com.altester.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePassDTO {
  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Verification code cannot be empty")
  @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be a 6-digit number")
  private String verificationCode;

  @NotBlank(message = "New password cannot be empty")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String newPassword;
}
