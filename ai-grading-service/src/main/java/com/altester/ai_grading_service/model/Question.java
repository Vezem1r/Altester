package com.altester.ai_grading_service.model;

import com.altester.ai_grading_service.model.enums.QuestionType;
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

  @Id private long id;

  @Column(name = "question_text", nullable = false)
  private String questionText;

  @Column(name = "image_path")
  private String imagePath;

  @Column(nullable = false)
  private int score;

  @Column(name = "correct_answer", length = 1000)
  private String correctAnswer;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private QuestionType questionType;

  @Column(nullable = false)
  private String difficulty;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Option> options = new ArrayList<>();
}
