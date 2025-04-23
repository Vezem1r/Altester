package com.altester.chat_service.repository;

import com.altester.chat_service.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversationIdOrderByTimestampDesc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.read = false AND m.senderId = :senderId")
    long countUnreadMessagesByConversation(@Param("conversationId") Long conversationId, @Param("senderId") String senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.read = false")
    int markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("senderId") String senderId);

    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.read = true AND m.timestamp < :beforeDate")
    int deleteOldReadMessages(@Param("beforeDate") LocalDateTime beforeDate);
}