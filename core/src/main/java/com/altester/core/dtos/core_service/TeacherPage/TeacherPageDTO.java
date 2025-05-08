package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherPageDTO {
    private String username;
    private String name;
    private String surname;
    private String email;
    private boolean isRegistered;
    private List<TeacherSubjectDTO> subjects;
}
