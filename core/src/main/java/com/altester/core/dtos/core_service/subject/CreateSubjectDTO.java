package com.altester.core.dtos.core_service.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSubjectDTO {

    @NotBlank(message = "Subject name is required")
    @Size(max = 63, message = "Subject name cannot exceed 63 characters")
    private String name;

    @NotBlank(message = "Short name is required")
    @Size(min = 2, max = 6, message = "Short name must be between 2 and 6 characters")
    private String shortName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
