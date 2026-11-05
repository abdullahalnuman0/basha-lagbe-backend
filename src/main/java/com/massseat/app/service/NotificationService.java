package com.massseat.app.service;

import com.massseat.app.dto.notification.NotificationResponse;
import com.massseat.app.entity.Notification;
import com.massseat.app.entity.Property;
import com.massseat.app.entity.User;
import com.massseat.app.entity.enums.NotificationType;
import com.massseat.app.entity.enums.NotyRefType;
import com.massseat.app.exception.ForbiddenException;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.repository.NotificationRepository;
import com.massseat.app.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;

    @Async
    @Transactional
    public void pendingPostApproval(List<Property> properties) {
        properties.forEach(property -> {
            notify(
                    property.getUser(),
                    "Property Approved Successfully!",
                    "Great news! Your property has been approved and is now live on MessSeat. Users can now discover your property and explore its rooms.",
                    NotificationType.PROPERTY_ACCEPTED,
                    NotyRefType.OWNER_POST,
                    property.getId()
            );
        });
    }

    @Transactional
    public void notify(User recipient, String title, String body, NotificationType type,
                       NotyRefType refType, Long refId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .title(title)
                .body(body)
                .type(type)
                .refType(refType)
                .refId(refId)
                .build();
        notificationRepository.save(notification);

        try {
            messagingTemplate.convertAndSend(
                    "/topic/users/" + recipient.getId() + "/notifications",
                    NotificationResponse.from(notification)
            );
        } catch (Exception ex) {
            log.warn("Failed to push notification over WebSocket: {}", ex.getMessage());
        }

        if (!recipient.isOnline()) {
            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());
            if (refType != null) {
                data.put("refType", refType.name());
            }
            if (refId != null) {
                data.put("refId", String.valueOf(refId));
            }
            fcmService.sendPush(recipient.getFcmToken(), title, body == null ? "" : body, data);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> myNotifications(Long userId, Pageable pageable) {
        return PageResponse.of(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable),
                NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This notification does not belong to you");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}
