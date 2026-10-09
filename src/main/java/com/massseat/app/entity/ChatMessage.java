package com.massseat.app.entity;

import com.massseat.app.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_message_chat", columnList = "chat_id")
})
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(length = 4000)
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageType type = MessageType.TEXT;

    @Column(length = 500)
    private String attachmentUrl;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean flagged = false;
}
