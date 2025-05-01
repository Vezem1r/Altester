package com.altester.ai_grading_service.repository;

import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAttempt(Attempt attempt);
    List<Submission> findByAttemptAndAiGraded(Attempt attempt, boolean aiGraded);
    List<Submission> findByAttemptIdAndAiGraded(Long attemptId, boolean aiGraded);
}