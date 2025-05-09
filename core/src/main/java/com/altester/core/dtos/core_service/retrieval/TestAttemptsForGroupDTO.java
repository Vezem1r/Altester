package com.altester.core.dtos.core_service.retrieval;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptsForGroupDTO {
  private Long groupId;
  private String groupName;
  private List<StudentAttemptGroup> students;
}
