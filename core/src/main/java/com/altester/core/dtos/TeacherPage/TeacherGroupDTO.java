package com.altester.core.dtos.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherGroupDTO {
    private String groupName;
    private int studentCount;
    private int testCount;
    private boolean active;
}


