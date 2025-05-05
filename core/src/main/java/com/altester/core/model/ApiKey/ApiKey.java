package com.altester.core.model.ApiKey;

import com.altester.core.model.ApiKey.enums.AiServiceName;
import com.altester.core.model.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @JsonIgnore
    private String encryptedKey;

    @Column(nullable = false)
    private String keyPrefix;

    @Column(nullable = false)
    private String keySuffix;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiServiceName aiServiceName;

    @Column
    private String model;

    @Column(nullable = false)
    private boolean isGlobal;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;
}
