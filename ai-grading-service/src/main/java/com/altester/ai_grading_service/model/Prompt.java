package com.altester.ai_grading_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {

    @Id
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Version
    private Long version;
}
