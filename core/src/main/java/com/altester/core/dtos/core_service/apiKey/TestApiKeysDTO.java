package com.altester.core.dtos.core_service.apiKey;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestApiKeysDTO {
  private List<ApiKeyAssignmentDTO> assignments;
}
