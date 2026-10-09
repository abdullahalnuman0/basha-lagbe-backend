package com.massseat.app.controller;

import com.massseat.app.dto.notification.NotificationResponse;
import com.massseat.app.security.UserPrincipal;
import com.massseat.app.service.NotificationService;
import com.massseat.app.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications (also pushed over WebSocket)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications")
    public PageResponse<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                                   @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return notificationService.myNotifications(principal.getId(), pageable);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count of my unread notifications")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("unread", notificationService.unreadCount(principal.getId()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public Map<String, String> markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        notificationService.markRead(id, principal.getId());
        return Map.of("message", "Marked as read");
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    public Map<String, String> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllRead(principal.getId());
        return Map.of("message", "All marked as read");
    }
}
