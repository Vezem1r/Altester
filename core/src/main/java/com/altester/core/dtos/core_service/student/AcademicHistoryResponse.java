package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicHistoryResponse {
    private String username;
    private String name;
    private String surname;
    private List<AcademicHistoryDTO> academicHistory;
}