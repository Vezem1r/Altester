package com.altester.core.repository;

import com.altester.core.model.subject.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAttemptId(Long attemptId);

    List<Submission> findByQuestionIdAndAttemptStudentId(Long questionId, Long studentId);
}
