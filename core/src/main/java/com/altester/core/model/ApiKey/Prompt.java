package com.altester.core.model.ApiKey;

import com.altester.core.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column
    private LocalDateTime lastModified;

    @Version
    private Long version;
}
