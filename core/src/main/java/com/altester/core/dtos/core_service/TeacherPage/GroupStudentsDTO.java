package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupStudentsDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
