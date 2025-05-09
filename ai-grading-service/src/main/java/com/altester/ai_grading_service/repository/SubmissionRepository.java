package com.altester.ai_grading_service.repository;

import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
  List<Submission> findByAttemptAndAiGraded(Attempt attempt, boolean aiGraded);
}
