package com.altester.core.dtos.core_service.subject;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectDTO {
  private long id;
  private String name;
  private String shortName;
  private String description;
  private LocalDateTime modified;
  private List<SubjectGroupDTO> groups;
}
