package com.altester.core.dtos.core_service.TeacherPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherGroupDTO {
  private String groupName;
  private int studentCount;
  private int testCount;
  private boolean active;
}
