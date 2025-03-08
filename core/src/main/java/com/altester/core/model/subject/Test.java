package com.altester.core.model.subject;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int max_attempts;

    @Column(nullable = false)
    private boolean isOpen;

    @Column()
    private LocalDateTime startTime;

    @Column()
    private LocalDateTime endTime;
}
