package com.altester.core.dtos.core_service.student;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegradeRequestDTO {

  @NotEmpty(message = "Submission request list cannot be empty")
  private List<Long> submissionIds;
}
