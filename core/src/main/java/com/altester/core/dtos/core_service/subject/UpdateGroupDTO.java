package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupDTO {

    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Student IDs are required")
    private Set<Long> studentsIds;

    private Semester semester;

    private Integer academicYear;

    private Boolean active;
}