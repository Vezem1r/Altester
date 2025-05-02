package com.altester.core.model.subject;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "answer_text", length = 4096)
    private String answerText;

    @Column
    private Integer score;

    @Column(length = 1024)
    private String teacherFeedback;

    @Column
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToMany
    @JoinTable(
            name = "submission_selected_options",
            joinColumns = @JoinColumn(name = "submission_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<Option> selectedOptions = new ArrayList<>();

    @Column(nullable = false)
    private boolean aiGraded = false;
}