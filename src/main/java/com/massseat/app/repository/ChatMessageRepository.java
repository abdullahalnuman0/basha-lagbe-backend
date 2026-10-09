package com.massseat.app.repository;

import com.massseat.app.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    Optional<ChatMessage> findTopByChatIdOrderByCreatedAtDesc(Long chatId);

    @Query("""
            select count(m) from ChatMessage m
            where m.chat.id = :chatId and m.sender.id <> :userId and m.read = false
            """)
    long countUnread(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Modifying
    @Query("""
            update ChatMessage m set m.read = true
            where m.chat.id = :chatId and m.sender.id <> :userId and m.read = false
            """)
    int markRead(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Query("""
            select count(m) from ChatMessage m
            where (m.chat.userA.id = :userId or m.chat.userB.id = :userId)
              and m.sender.id <> :userId and m.read = false
            """)
    long countTotalUnreadForUser(@Param("userId") Long userId);
}
