package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SubjectDTO {
    private long id;
    private String name;
    private String shortName;
    private String description;
    private LocalDateTime modified;
    private List<SubjectGroupDTO> groups;
}
