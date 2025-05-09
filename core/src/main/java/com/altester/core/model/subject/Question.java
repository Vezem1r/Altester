package com.altester.core.model.subject;

import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.model.subject.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "question_text", nullable = false)
  private String questionText;

  @Column(name = "image_path")
  private String imagePath;

  @Column(nullable = false)
  private int score;

  @Column(name = "correct_answer", length = 1000)
  private String correctAnswer;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuestionType questionType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuestionDifficulty difficulty = QuestionDifficulty.MEDIUM;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  @JsonBackReference
  private Test test;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Option> options = new ArrayList<>();

  @OneToMany(mappedBy = "question")
  @JsonBackReference
  private List<Submission> submissions = new ArrayList<>();
}
