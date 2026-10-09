package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.dto.chat.ChatResponse;
import com.massseat.app.dto.chat.MessageResponse;
import com.massseat.app.dto.chat.SendMessageRequest;
import com.massseat.app.entity.Chat;
import com.massseat.app.entity.ChatMessage;
import com.massseat.app.entity.Room;
import com.massseat.app.entity.User;
import com.massseat.app.entity.enums.MessageType;
import com.massseat.app.entity.enums.NotificationType;
import com.massseat.app.entity.enums.NotyRefType;
import com.massseat.app.exception.BadRequestException;
import com.massseat.app.exception.ForbiddenException;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.repository.*;
import com.massseat.app.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final List<String> FLAGGED_KEYWORDS = List.of("scam", "fraud", "prostitut", "drug");

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppProperties appProperties;
    private final FileStorageService fileService;


    @Transactional
    public Chat getOrCreateChat(User fast, User second, Long contextId) {
        User a = fast.getId() < second.getId() ? fast : second;
        User b = fast.getId() < second.getId() ? second : fast;
        return chatRepository
                .findByUserA_IdAndUserB_IdAndContextId(a.getId(), b.getId(), contextId)
                .orElseGet(() -> chatRepository.save(
                        Chat.builder()
                                .userA(a)
                                .userB(b)
                                .contextId(contextId)
                                .lastMessageAt(Instant.now())
                                .build()
                ));
    }

    @Transactional
    public ChatResponse openChat(Long userId, Long contextId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Room room = roomRepository.findById(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", contextId));

        User other = room.getProperty().getUser();
        if (other.getId().equals(me.getId()))
            throw new BadRequestException("Open this chat from your messages list");

        Chat chat = getOrCreateChat(me, other, contextId);
        return toChatResponse(chat, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatResponse> myChats(Long userId, Pageable pageable) {
        return PageResponse.of(
                chatRepository.findAllForUser(userId, pageable),
                chat -> toChatResponse(chat, userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> messages(Long chatId, Long userId, Pageable pageable) {
        Chat chat = participantChat(chatId, userId);
        return PageResponse.of(
                messageRepository.findByChatIdOrderByCreatedAtDesc(chat.getId(), pageable),
                MessageResponse::from);
    }

    @Transactional
    public MessageResponse sendMessage(Long chatId, Long senderId, SendMessageRequest request) {
        Chat chat = participantChat(chatId, senderId);
        boolean hasText = request.getContent() != null && !request.getContent().isBlank();
        boolean hasAttachment = request.getAttachmentUrl() != null && !request.getAttachmentUrl().isBlank();
        if (!hasText && !hasAttachment) {
            throw new BadRequestException("Message must contain text or an attachment");
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType() == null ? (hasAttachment ? MessageType.IMAGE : MessageType.TEXT)
                        : request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .flagged(isFlagged(request.getContent()))
                .build();
        messageRepository.save(message);

        chat.setLastMessageAt(Instant.now());
        chatRepository.save(chat);

        MessageResponse response = MessageResponse.from(message);
        // Real-time delivery to both participants subscribed to this chat topic
        try {
            messagingTemplate.convertAndSend("/topic/chats/" + chat.getId(), response);
        } catch (Exception ex) {
            log.warn("WebSocket delivery failed for chat {}: {}", chat.getId(), ex.getMessage());
        }
        User receiver = chat.otherParticipant(senderId);
        notificationService.notify(receiver, "New message",
                sender.getFullName() + ": " + preview(request.getContent()),
                NotificationType.NEW_MESSAGE, NotyRefType.CHAT, chat.getId());

        // remove file from track
        if (message.getType() == MessageType.IMAGE)
            fileService.removeFromTrack(List.of(message.getAttachmentUrl()));

        return response;
    }

    @Transactional
    public void markRead(Long chatId, Long userId) {
        participantChat(chatId, userId);
        messageRepository.markRead(chatId, userId);
    }

    @Transactional(readOnly = true)
    public long totalUnread(Long userId) {
        return messageRepository.countTotalUnreadForUser(userId);
    }

    @Transactional(readOnly = true)
    public boolean chatExists(Long firstUserId, Long secondUserId, Long contextId) {
        long a = Math.min(firstUserId, secondUserId);
        long b = Math.max(firstUserId, secondUserId);
        return chatRepository.findByUserA_IdAndUserB_IdAndContextId(a, b, contextId).isPresent();
    }


    private Chat participantChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        if (!chat.hasParticipant(userId)) {
            throw new ForbiddenException("You are not a participant of this chat");
        }
        return chat;
    }

    private ChatResponse toChatResponse(Chat chat, Long userId) {
        String preview = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.getId())
                .map(m -> m.getContent() != null ? preview(m.getContent()) : "[attachment]")
                .orElse(null);
        long unread = messageRepository.countUnread(chat.getId(), userId);
        return ChatResponse.from(chat, userId, preview, unread);
    }

    private String preview(String content) {
        if (content == null) {
            return "[attachment]";
        }
        return content.length() > 80 ? content.substring(0, 77) + "..." : content;
    }

    private boolean isFlagged(String content) {
        if (content == null) {
            return false;
        }
        String lower = content.toLowerCase(Locale.ROOT);
        return FLAGGED_KEYWORDS.stream().anyMatch(lower::contains);
    }


}
