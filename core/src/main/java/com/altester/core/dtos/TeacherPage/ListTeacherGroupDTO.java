package com.altester.core.dtos.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListTeacherGroupDTO {
    private long id;
    private String name;
    private String subjectName;
    private List<GroupStudentsDTO> students;
    private boolean active;
}
