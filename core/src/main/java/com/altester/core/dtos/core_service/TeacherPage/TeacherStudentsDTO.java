package com.altester.core.dtos.core_service.TeacherPage;

import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TeacherStudentsDTO {
        private String firstName;
        private String lastName;
        private String email;
        private String username;
        private List<SubjectGroupDTO> subjectGroups;
        private LocalDateTime lastLogin;
}
