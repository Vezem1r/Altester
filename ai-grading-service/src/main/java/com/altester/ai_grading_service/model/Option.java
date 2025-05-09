package com.altester.ai_grading_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "options")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Option {

  @Id private long id;

  @Column(nullable = false)
  private String text;

  @Column(length = 1024)
  private String description;

  @Column(nullable = false)
  private boolean isCorrect;

  @ManyToOne
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;
}
