package com.altester.core.dtos.core_service.AdminPage;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUser {

  @NotBlank(message = "Name is required")
  @Size(max = 63, message = "Name cannot exceed 63 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Size(max = 63, message = "Email cannot exceed 63 characters")
  private String email;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 7, message = "Username must be between 3 and 7 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9_]*$",
      message = "Username can only contain letters," + " numbers, and underscores")
  private String username;

  @NotBlank(message = "Last name is required")
  @Size(max = 127, message = "Last name cannot exceed 127 characters")
  private String lastname;
}
