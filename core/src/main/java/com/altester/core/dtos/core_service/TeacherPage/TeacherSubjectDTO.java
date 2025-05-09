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
public class TeacherSubjectDTO {
  private String name;
  private String shortName;
  private String description;
  private List<TeacherGroupDTO> groups;
}
