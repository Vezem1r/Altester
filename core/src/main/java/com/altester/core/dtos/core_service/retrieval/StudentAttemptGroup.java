package com.altester.core.dtos.core_service.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttemptGroup {
    private String username;
    private String firstName;
    private String lastName;
    private List<AttemptInfoDTO> attempts;
}