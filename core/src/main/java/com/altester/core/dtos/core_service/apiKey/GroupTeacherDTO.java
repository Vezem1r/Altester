package com.altester.core.dtos.core_service.apiKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupTeacherDTO {
    private Long groupId;
    private String groupName;
    private String teacherUsername;
}
