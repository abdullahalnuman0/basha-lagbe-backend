package com.massseat.app.entity;

import com.massseat.app.entity.enums.NotificationType;
import com.massseat.app.entity.enums.NotyRefType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id")
})
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotyRefType refType;

    private Long refId;
}
