package com.altester.ai_grading_service.repository;

import com.altester.ai_grading_service.model.Attempt;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
  @NonNull
  Optional<Attempt> findById(@NonNull Long id);
}
