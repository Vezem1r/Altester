package com.altester.ai_grading_service.repository;

import com.altester.ai_grading_service.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    Optional<Attempt> findById(Long id);
}