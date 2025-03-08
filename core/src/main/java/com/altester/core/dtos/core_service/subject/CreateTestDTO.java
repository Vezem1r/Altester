package com.altester.core.dtos.core_service.subject;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTestDTO {

    @NotNull(message = "Group ID cannot be null")
    private long groupId;

    @NotBlank(message = "Title is required")
    @Size(min = 4, max = 64, message = "Title should be between 4 and 64 characters")
    private String title;

    @Size(max = 1024, message = "Description should not exceed 1024 characters")
    private String description;

    private int duration;

    @Min(value = 0, message = "Score cannot be negative")
    private int score;

    @Min(value = 1, message = "Max attempts must be at least 1")
    private int max_attempts;

    private boolean isOpen;

    private LocalDateTime startTime;

    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}
