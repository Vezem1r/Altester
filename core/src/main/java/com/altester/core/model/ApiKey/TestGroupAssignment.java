package com.altester.core.model.ApiKey;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "test_group_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"test_id", "group_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestGroupAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @ManyToOne
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @ManyToOne
  @JoinColumn(name = "api_key_id")
  private ApiKey apiKey;

  @ManyToOne
  @JoinColumn(name = "prompt_id")
  private Prompt prompt;

  @Column(nullable = false)
  private LocalDateTime assignedAt;

  @ManyToOne
  @JoinColumn(name = "assigned_by_id", nullable = false)
  private User assignedBy;

  @Column(nullable = false)
  private boolean aiEvaluation = false;
}
