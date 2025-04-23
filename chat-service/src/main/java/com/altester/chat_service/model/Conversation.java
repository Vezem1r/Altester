package com.altester.chat_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"participant1_id", "participant2_id"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant1_id", nullable = false)
    private String participant1Id;

    @Column(name = "participant2_id", nullable = false)
    private String participant2Id;

    @Column(nullable = false)
    private LocalDateTime lastMessageTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    public boolean hasParticipant(String userId) {
        return participant1Id.equals(userId) || participant2Id.equals(userId);
    }

    public String getOtherParticipantId(String userId) {
        if (participant1Id.equals(userId)) {
            return participant2Id;
        } else if (participant2Id.equals(userId)) {
            return participant1Id;
        }
        throw new IllegalArgumentException("User is not a participant in this conversation");
    }
}
