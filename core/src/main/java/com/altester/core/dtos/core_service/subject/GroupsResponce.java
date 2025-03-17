package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupsResponce {
    private Long id;
    private String name;
    private String teacherUsername;
    private int studentCount;
    private String subjectShortName;

    private Semester semester;
    private Integer academicYear;
    private boolean active;
}
