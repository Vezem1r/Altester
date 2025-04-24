package com.altester.core.dtos.core_service.TeacherPage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveStudentRequest {

    @NotBlank(message = "Student username is required")
    private String studentUsername;

    @NotNull(message = "Source group ID is required")
    @Positive(message = "Source group ID must be positive")
    private Long fromGroupId;

    @NotNull(message = "Target group ID is required")
    @Positive(message = "Target group ID must be positive")
    private Long toGroupId;
}