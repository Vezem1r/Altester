package com.altester.core.dtos.core_service.student;

import com.altester.core.model.subject.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicPeriod {
  private Integer academicYear;
  private Semester semester;
}
