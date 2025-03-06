package com.altester.core.dtos.core_service.subject;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateGroupsDTO {
    private long subjectId;
    private Set<Long> groupIds;
}
