package com.altester.core.dtos.core_service.student;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailablePeriodsResponse {
  private String username;
  private List<AcademicPeriod> periods;
}
