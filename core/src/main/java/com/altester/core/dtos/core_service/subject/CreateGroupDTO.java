package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDTO {

  @NotBlank(message = "Group name is required")
  @Size(max = 255, message = "Group name cannot exceed 255 characters")
  private String groupName;

  @NotNull(message = "Teacher ID is required")
  @Min(value = 1, message = "Teacher ID must be positive")
  private Long teacherId;

  @NotNull(message = "Subject ID is required")
  private Long subjectId;

  private Semester semester;

  @Min(value = 2000, message = "Academic year must be valid (at least 2000)")
  private Integer academicYear;

  private Boolean active;
}
