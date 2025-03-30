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
public class AvailablePeriodsResponse {
    private String username;
    private List<AcademicPeriod> periods;
}