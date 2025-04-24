package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTeacherGroupDTO {
    private long id;
    private String name;
    private String subjectName;
    private List<GroupStudentsDTO> students;
    private boolean active;
    private boolean future;
}
