package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDTO {
    private String groupName;
    private Long teacherId;
    private Set<Long> studentsIds;
    private Semester semester;
    private Integer academicYear;
    private Boolean active;
}
