package com.altester.auth.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDTO {
  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 63, message = "Name must be between 2 and 50 characters")
  private String name;

  @NotBlank(message = "Surname is required")
  @Size(min = 2, max = 127, message = "Surname must be between 2 and 50 characters")
  private String surname;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
      message = "Password must contain at least one digit, one lowercase and one uppercase letter")
  private String password;
}
