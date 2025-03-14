package com.altester.core.dtos.core_service.subject;

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
}