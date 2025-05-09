package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupStudentsDTO {
  private String username;
  private String firstName;
  private String lastName;
  private String email;
}
