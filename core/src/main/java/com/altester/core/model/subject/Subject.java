package com.altester.core.model.subject;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subject")
public class Subject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(length = 63, nullable = false)
  private String name;

  @Column(length = 6, nullable = false, unique = true)
  private String shortName;

  @Column(length = 255)
  private String description;

  @Column private LocalDateTime modified;

  @OneToMany
  @JoinColumn(name = "subject_id")
  private Set<Group> groups = new HashSet<>();
}
