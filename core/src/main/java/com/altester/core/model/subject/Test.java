package com.altester.core.model.subject;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(exclude = {"questions", "attempts"})
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private boolean isOpen;

    @Column
    private Integer maxAttempts;

    @Column
    private Integer maxQuestions;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean isCreatedByAdmin = false;

    @Column(nullable = false)
    private boolean allowTeacherEdit = false;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Attempt> attempts = new ArrayList<>();

    @Transient
    public int getTotalScore() {
        return questions.stream().mapToInt(Question::getScore).sum();
    }
}