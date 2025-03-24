package com.altester.core.dtos.core_service.subject;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class GroupStudentsResponseDTO {
    private List<CreateGroupUserListDTO> currentMembers;
    private Page<CreateGroupUserListDTO> availableStudents;
}
