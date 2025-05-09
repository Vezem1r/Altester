package com.altester.core.dtos.core_service.TeacherPage;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherStudentsDTO {
  private String firstName;
  private String lastName;
  private String email;
  private String username;
  private List<SubjectGroupDTO> subjectGroups;
  private LocalDateTime lastLogin;
}
