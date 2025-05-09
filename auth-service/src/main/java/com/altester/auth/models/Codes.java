package com.altester.auth.models;

import com.altester.auth.models.enums.CodeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "codes")
public class Codes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false)
  @JsonIgnore
  private String code;

  @Column(name = "expiration", nullable = false)
  @JsonIgnore
  private LocalDateTime expiration;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "code_type", nullable = false)
  private CodeType codeType;

  @Column(name = "sendAt")
  @JsonIgnore
  private LocalDateTime sendAt;
}
