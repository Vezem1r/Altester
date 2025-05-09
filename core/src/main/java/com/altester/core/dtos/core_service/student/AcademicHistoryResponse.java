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
public class AcademicHistoryResponse {
  private String username;
  private String name;
  private String surname;
  private List<AcademicHistoryDTO> academicHistory;
}
