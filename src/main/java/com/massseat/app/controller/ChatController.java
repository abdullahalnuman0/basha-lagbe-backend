package com.massseat.app.controller;

import com.massseat.app.dto.chat.ChatResponse;
import com.massseat.app.dto.chat.MessageResponse;
import com.massseat.app.dto.chat.OpenChatRequest;
import com.massseat.app.dto.chat.SendMessageRequest;
import com.massseat.app.security.UserPrincipal;
import com.massseat.app.service.ChatService;
import com.massseat.app.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = """
        In-app chat. Send via REST; subscribe to /topic/chats/{chatId} over STOMP WebSocket (/ws endpoint) \
        for real-time delivery. Notifications stream on /topic/users/{userId}/notifications.""")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/open")
    @Operation(summary = "Open (or fetch) a chat for a book post or an accepted tuition application")
    public ResponseEntity<ChatResponse> open(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody OpenChatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.openChat(principal.getId(), request.getContextId()));
    }

    @GetMapping
    @Operation(summary = "List my chats with last message & unread counts")
    public PageResponse<ChatResponse> myChats(@AuthenticationPrincipal UserPrincipal principal,
                                              @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return chatService.myChats(principal.getId(), pageable);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get messages of a chat (newest first)")
    public PageResponse<MessageResponse> messages(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long id,
                                                  @ParameterObject @PageableDefault(size = 30) Pageable pageable) {
        return chatService.messages(id, principal.getId(), pageable);
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Send a message (text / image / file URL); broadcast over WebSocket")
    public ResponseEntity<MessageResponse> send(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(id, principal.getId(), request));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark all messages in a chat as read")
    public Map<String, String> markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        chatService.markRead(id, principal.getId());
        return Map.of("message", "Marked as read");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Total unread messages across all my chats")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("unread", chatService.totalUnread(principal.getId()));
    }
}
