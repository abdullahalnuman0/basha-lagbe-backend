package com.massseat.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "chats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_participants_context",
                        columnNames = {"user_a_id", "user_b_id", "context_id"})
        }
)
public class Chat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(name = "context_id", nullable = false)
    private Long contextId;

    private Instant lastMessageAt;

    public boolean hasParticipant(Long userId) {
        return userA.getId().equals(userId) || userB.getId().equals(userId);
    }

    public User otherParticipant(Long userId) {
        return userA.getId().equals(userId) ? userB : userA;
    }
}