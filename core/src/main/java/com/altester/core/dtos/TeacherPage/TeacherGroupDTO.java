package com.altester.core.dtos.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherGroupDTO {
    private String groupName;
    private int studentCount;
    private int testCount;
}
