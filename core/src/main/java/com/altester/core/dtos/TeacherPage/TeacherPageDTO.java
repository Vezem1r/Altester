package com.altester.core.dtos.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherPageDTO {
    private String username;
    private boolean isRegistered;
    private List<TeacherSubjectDTO> subjects;
}
