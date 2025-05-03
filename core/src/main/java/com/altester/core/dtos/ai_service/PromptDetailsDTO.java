package com.altester.core.dtos.ai_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptDetailsDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private boolean isPublic;
    private String authorUsername;
    private LocalDateTime created;
    private LocalDateTime lastModified;
}
