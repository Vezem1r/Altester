package com.altester.ai_grading_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Test {

    @Id
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1024)
    private String description;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Question> questions = new HashSet<>();
}
