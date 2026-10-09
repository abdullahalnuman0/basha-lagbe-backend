package com.massseat.app.dto.notification;

import com.massseat.app.entity.Notification;
import com.massseat.app.entity.enums.NotificationType;
import com.massseat.app.entity.enums.NotyRefType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private boolean read;
    private NotyRefType refType;
    private Long refId;
    private Instant createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .read(notification.isRead())
                .refType(notification.getRefType())
                .refId(notification.getRefId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

