package com.altester.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailResendDTO {

  @Email @NotBlank private String email;

  @NotBlank private String username;
}
