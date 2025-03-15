package com.altester.core.dtos.AdminPage;

import lombok.Data;

@Data
public class AdminPageDTO {
    private long studentsCount;
    private long groupsCount;
    private long teachersCount;
    private long subjectsCount;
    private long testsCount;
    private String username;
}
