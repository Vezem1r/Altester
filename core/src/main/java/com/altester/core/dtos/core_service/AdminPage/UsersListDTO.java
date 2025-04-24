package com.altester.core.dtos.core_service.AdminPage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersListDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastLogin;

    private boolean registered;

    private List<String> subjectShortNames = new ArrayList<>();
    private List<String> groupNames = new ArrayList<>();
}