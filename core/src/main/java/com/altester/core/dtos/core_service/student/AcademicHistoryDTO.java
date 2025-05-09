package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.Semester;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicHistoryDTO {
  private Semester semester;
  private Integer academicYear;
  private List<GroupDTO> groups;
}
