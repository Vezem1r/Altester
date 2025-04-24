package com.altester.core.dtos.core_service.subject;

import com.altester.core.util.CacheablePage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupStudentsResponseDTO {
    private List<CreateGroupUserListDTO> currentMembers;
    private CacheablePage<CreateGroupUserListDTO> availableStudents;
}
