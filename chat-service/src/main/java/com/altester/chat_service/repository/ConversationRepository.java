package com.altester.chat_service.repository;

import com.altester.chat_service.model.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  @Query(
      "SELECT c FROM Conversation c WHERE "
          + "(c.participant1Id = :participant1Id AND c.participant2Id = :participant2Id) OR "
          + "(c.participant1Id = :participant2Id AND c.participant2Id = :participant1Id)")
  Optional<Conversation> findConversationBetween(
      @Param("participant1Id") String participant1Id,
      @Param("participant2Id") String participant2Id);

  @Query(
      "SELECT c FROM Conversation c WHERE c.participant1Id = :userId OR c.participant2Id = :userId "
          + "ORDER BY c.lastMessageTime DESC")
  List<Conversation> findConversationsForUser(@Param("userId") String userId);

  @Query(
      "SELECT c FROM Conversation c WHERE c.participant1Id = :userId OR c.participant2Id = :userId "
          + "ORDER BY c.lastMessageTime DESC")
  Page<Conversation> findConversationsForUserPaginated(
      @Param("userId") String userId, Pageable pageable);
}
