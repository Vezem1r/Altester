package com.altester.core.dtos.core_service.AdminPage;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsersListDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private LocalDateTime lastLogin;
    private boolean isRegistered;
    private List<String> subjectShortNames;
    private List<String> groupNames;
}
