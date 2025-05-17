package com.altester.ai_grading_service.model;

import com.altester.ai_grading_service.model.enums.AttemptStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attempts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Attempt {

  @Id private long id;

  @Column(nullable = false)
  private int attemptNumber;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column private LocalDateTime endTime;

  @Column private Integer aiScore;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AttemptStatus status;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @Column(name = "student_id", nullable = false)
  private Long studentId;

  @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Submission> submissions = new ArrayList<>();
}
