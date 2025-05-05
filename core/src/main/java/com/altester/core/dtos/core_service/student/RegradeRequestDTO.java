package com.altester.core.dtos.core_service.student;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegradeRequestDTO {

    @NotEmpty(message = "Submission request list cannot be empty")
    private List<Long> submissionIds;
}
