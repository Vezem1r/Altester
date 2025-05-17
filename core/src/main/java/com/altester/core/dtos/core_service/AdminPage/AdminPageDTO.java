package com.altester.core.dtos.core_service.AdminPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminPageDTO {
  private long studentsCount;
  private long groupsCount;
  private long teachersCount;
  private long subjectsCount;
  private long testsCount;
  private double aiAccuracy;
  private String username;
}
