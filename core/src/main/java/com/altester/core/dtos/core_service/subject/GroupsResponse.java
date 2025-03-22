package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupsResponse {
    private Long id;
    private String name;
    private String teacherUsername;
    private int studentCount;
    private String subjectShortName;
    private Semester semester;
    private Integer academicYear;
    private boolean active;
    private boolean isInFuture;

    public GroupsResponse(Long id, String name, String teacherUsername, int studentCount,
                          String subjectShortName, Semester semester, Integer academicYear,
                          boolean active) {
        this.id = id;
        this.name = name;
        this.teacherUsername = teacherUsername;
        this.studentCount = studentCount;
        this.subjectShortName = subjectShortName;
        this.semester = semester;
        this.academicYear = academicYear;
        this.active = active;
        this.isInFuture = false;
    }
}