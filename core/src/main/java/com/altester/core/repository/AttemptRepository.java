package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
  List<Attempt> findByTestAndStudent(Test test, User student);

  int countByEndTimeAfter(LocalDateTime date);

  @Query("SELECT a FROM Attempt a LEFT JOIN FETCH a.submissions WHERE a.id = :id")
  Optional<Attempt> findByIdWithSubmissions(@Param("id") Long id);

  @Query("SELECT a FROM Attempt a WHERE a.score IS NOT NULL AND a.aiScore IS NOT NULL")
  List<Attempt> findAllWithBothScores();
}
