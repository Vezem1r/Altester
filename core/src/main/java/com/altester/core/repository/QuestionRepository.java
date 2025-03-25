package com.altester.core.repository;

import com.altester.core.model.subject.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT MAX(q.position) FROM Question q WHERE q.test.id = :testId")
    Optional<Integer> findMaxPositionByTestId(@Param("testId") Long testId);

    @Modifying
    @Query("UPDATE Question q SET q.position = q.position + 1 WHERE q.test.id = :testId AND q.position >= :startPosition AND q.position <= :endPosition")
    void incrementPositionForRange(@Param("testId") Long testId, @Param("startPosition") int startPosition, @Param("endPosition") int endPosition);

    @Modifying
    @Query("UPDATE Question q SET q.position = q.position - 1 WHERE q.test.id = :testId AND q.position >= :startPosition AND q.position <= :endPosition")
    void decrementPositionForRange(@Param("testId") Long testId, @Param("startPosition") int startPosition, @Param("endPosition") int endPosition);
}