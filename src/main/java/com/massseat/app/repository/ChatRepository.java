package com.massseat.app.repository;

import com.massseat.app.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByUserA_IdAndUserB_IdAndContextId(Long userAId, Long userBId, Long contextId);

    @Query("""
            select c from Chat c
            where c.userA.id = :userId or c.userB.id = :userId
            order by c.lastMessageAt desc
            """)
    Page<Chat> findAllForUser(@Param("userId") Long userId, Pageable pageable);
}
