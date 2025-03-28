package com.altester.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailConfirmDTO {
    @NotNull
    private Long userId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String emailCode;
}
