package com.altester.core.dtos.core_service.TeacherPage;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private double aiAccuracy;
  private int students;
  private int tests;
  private List<TeacherSubjectDTO> subjects;
}
