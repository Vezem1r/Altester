package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicHistoryDTO {
    private Semester semester;
    private Integer academicYear;
    private List<GroupDTO> groups;
}