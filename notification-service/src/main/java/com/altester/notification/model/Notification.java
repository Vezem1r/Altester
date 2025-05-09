package com.altester.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String username;

  @Column private String title;

  @Column(length = 1000)
  private String message;

  @Column(nullable = false)
  private boolean read;

  @Column(nullable = false)
  private String type;

  @Column private String actionUrl;

  @Column private Long referenceId;

  @Column(nullable = false)
  private LocalDateTime createdAt;
}
