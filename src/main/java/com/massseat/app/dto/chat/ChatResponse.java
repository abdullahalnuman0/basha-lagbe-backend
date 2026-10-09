package com.massseat.app.dto.chat;

import com.massseat.app.entity.Chat;
import com.massseat.app.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Builder
public class ChatResponse {
    private Long id;
    private Long contextId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private long unreadCount;
    private Instant createdAt;

    public static ChatResponse from(Chat chat, Long currentUserId, String lastMessagePreview, long unreadCount) {
        User other = chat.otherParticipant(currentUserId);
        return ChatResponse.builder()
                .id(chat.getId())
                .contextId(chat.getContextId())
                .otherUserId(other.getId())
                .otherUserName(other.getFullName())
                .otherUserAvatarUrl(other.getAvatarUrl())
                .lastMessagePreview(lastMessagePreview)
                .lastMessageAt(chat.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
