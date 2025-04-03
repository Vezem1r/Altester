package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDashboardResponse {
    private String username;
    private String name;
    private String surname;
    private String email;
    private boolean isRegistered;
    private List<GroupDTO> currentGroups;
}
