package com.altester.core.dtos.core_service.subject;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
public class UpdateGroupsDTO {

  @NotNull(message = "Subject ID is required")
  @Min(value = 1, message = "Subject ID must be positive")
  private long subjectId;

  @NotNull(message = "Group IDs set cannot be null")
  private Set<Long> groupIds;
}
