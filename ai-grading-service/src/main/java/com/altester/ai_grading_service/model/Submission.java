package com.altester.ai_grading_service.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Submission {

  @Id private long id;

  @Column(name = "answer_text", length = 4096)
  private String answerText;

  @Column private Integer aiScore;

  @Column(length = 2048)
  private String aiFeedback;

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
      inverseJoinColumns = @JoinColumn(name = "option_id"))
  @Builder.Default
  private List<Option> selectedOptions = new ArrayList<>();

  @Column(nullable = false)
  @Builder.Default
  private boolean aiGraded = false;
}
