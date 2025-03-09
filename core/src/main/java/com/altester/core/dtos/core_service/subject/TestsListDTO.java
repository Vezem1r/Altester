package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestsListDTO {
    private String title;
    private int duration;
    private int score;
    private int max_attempts;
    private boolean isOpen;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
