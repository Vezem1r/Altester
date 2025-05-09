package com.altester.core.dtos.core_service.subject;

import com.altester.core.util.CacheablePage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupStudentsResponseDTO {
  private List<CreateGroupUserListDTO> currentMembers;
  private CacheablePage<CreateGroupUserListDTO> availableStudents;
}
