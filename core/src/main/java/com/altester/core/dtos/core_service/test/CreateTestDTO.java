package com.altester.core.dtos.core_service.test;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTestDTO {
  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must be less than 255 characters")
  private String title;

  @Size(max = 1024, message = "Description must be less than 1024 characters")
  private String description;

  @Min(value = 1, message = "Duration must be at least 1 minute")
  private int duration;

  private Integer maxAttempts;

  @Min(value = 0, message = "Easy questions count cannot be negative")
  private Integer easyQuestionsCount;

  @Min(value = 0, message = "Medium questions count cannot be negative")
  private Integer mediumQuestionsCount;

  @Min(value = 0, message = "Hard questions count cannot be negative")
  private Integer hardQuestionsCount;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private Long subjectId;

  private Set<Long> groupIds;

  @AssertTrue(message = "End time must be after start time")
  private boolean isTimeRangeValid() {
    if (startTime == null || endTime == null) {
      return true;
    }
    return endTime.isAfter(startTime);
  }

  @AssertTrue(message = "At least one difficulty level must have a count greater than 0")
  private boolean isAtLeastOneDifficultySpecified() {
    return (easyQuestionsCount != null && easyQuestionsCount > 0)
        || (mediumQuestionsCount != null && mediumQuestionsCount > 0)
        || (hardQuestionsCount != null && hardQuestionsCount > 0);
  }
}
