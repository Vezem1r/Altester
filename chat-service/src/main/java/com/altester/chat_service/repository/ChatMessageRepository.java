package com.altester.chat_service.repository;

import com.altester.chat_service.model.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  @Modifying
  @Query("DELETE FROM ChatMessage m WHERE m.read = true AND m.timestamp < :beforeDate")
  int deleteOldReadMessages(@Param("beforeDate") LocalDateTime beforeDate);

  Page<ChatMessage> findByConversationIdOrderByTimestampDesc(
      Long conversationId, Pageable pageable);

  @Modifying
  @Query(
      "UPDATE ChatMessage m SET m.read = true WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.read = false")
  int markMessagesAsRead(
      @Param("conversationId") Long conversationId, @Param("senderId") String senderId);

  @Query(
      "SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.read = false")
  long countUnreadMessagesByConversation(
      @Param("conversationId") Long conversationId, @Param("senderId") String senderId);

  @Query(
      "SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.read = false ORDER BY m.timestamp ASC")
  List<ChatMessage> findFirstUnreadMessages(
      @Param("conversationId") Long conversationId, @Param("senderId") String senderId);

  @Query(
      "SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.read = true ORDER BY m.timestamp DESC")
  List<ChatMessage> findRecentlyMarkedAsRead(
      @Param("conversationId") Long conversationId,
      @Param("senderId") String senderId,
      Pageable pageable);

  default List<ChatMessage> findRecentlyMarkedAsRead(Long conversationId, String senderId) {
    return findRecentlyMarkedAsRead(conversationId, senderId, PageRequest.of(0, 20));
  }
}
