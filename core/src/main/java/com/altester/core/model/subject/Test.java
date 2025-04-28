package com.altester.core.model.subject;

import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "tests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(exclude = {"questions", "attempts", "testGroupAssignments"})
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
    private Integer easyQuestionsCount = 0;

    @Column
    private Integer mediumQuestionsCount = 0;

    @Column
    private Integer hardQuestionsCount = 0;

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

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    private Set<TestGroupAssignment> testGroupAssignments = new HashSet<>();

    @Transient
    public int getTotalQuestions() {
        int easyCount = (getEasyQuestionsCount() != null) ? getEasyQuestionsCount() : 0;
        int mediumCount = (getMediumQuestionsCount() != null) ? getMediumQuestionsCount() : 0;
        int hardCount = (getHardQuestionsCount() != null) ? getHardQuestionsCount() : 0;

        return easyCount + mediumCount + hardCount;
    }

    @Transient
    public int getTotalScore() {
        Map<QuestionDifficulty, List<Question>> grouped = questions.stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));

        Map<QuestionDifficulty, Integer> difficultyCountMap = Map.of(
                QuestionDifficulty.EASY, Optional.ofNullable(easyQuestionsCount).orElse(0),
                QuestionDifficulty.MEDIUM, Optional.ofNullable(mediumQuestionsCount).orElse(0),
                QuestionDifficulty.HARD, Optional.ofNullable(hardQuestionsCount).orElse(0)
        );

        return difficultyCountMap.entrySet().stream()
                .mapToInt(entry -> {
                    List<Question> list = grouped.getOrDefault(entry.getKey(), Collections.emptyList());
                    if (list.isEmpty() || entry.getValue() <= 0) return 0;
                    double avg = list.stream().mapToInt(Question::getScore).average().orElse(0);
                    return entry.getValue() * (int)Math.round(avg);
                })
                .sum();
    }
}