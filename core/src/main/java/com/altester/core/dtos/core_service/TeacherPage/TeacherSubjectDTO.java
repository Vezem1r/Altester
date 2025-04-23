package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherSubjectDTO {
    private String name;
    private String shortName;
    private String description;
    private List<TeacherGroupDTO> groups;
}
