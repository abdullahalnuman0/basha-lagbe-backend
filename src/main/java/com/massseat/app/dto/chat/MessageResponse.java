package com.massseat.app.dto.chat;

import com.massseat.app.entity.ChatMessage;
import com.massseat.app.entity.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class MessageResponse {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private String attachmentUrl;
    private boolean read;
    private Instant createdAt;

    public static MessageResponse from(ChatMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .content(message.getContent())
                .type(message.getType())
                .attachmentUrl(message.getAttachmentUrl())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

