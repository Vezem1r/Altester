package com.altester.core.dtos.core_service.subject;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GroupDTO {
    private long id;
    private String name;
    private String subject;
    private List<GroupUserList> students;
    private GroupUserList teacher;

    private Semester semester;
    private Integer academicYear;
    private boolean active;
}