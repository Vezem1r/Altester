package com.altester.core.dtos.core_service.subject;

import lombok.Data;

import java.util.Set;

@Data
public class CreateGroupDTO {
    private String groupName;
    private Long subjectId;
    private Long teacherId;
    private Set<Long> studentsIds;
}
