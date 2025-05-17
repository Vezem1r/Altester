package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.dto.SubmissionGradingResult;
import com.altester.ai_grading_service.exception.ResourceNotFoundException;
import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.model.enums.AttemptStatus;
import com.altester.ai_grading_service.repository.AttemptRepository;
import com.altester.ai_grading_service.repository.SubmissionRepository;
import com.altester.ai_grading_service.service.SubmissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

  private final SubmissionRepository submissionRepository;
  private final AttemptRepository attemptRepository;

  @Override
  public List<Submission> getSubmissionsForAiGrading(Attempt attempt) {
    return submissionRepository.findByAttemptAndAiGraded(attempt, false);
  }

  @Override
  @Transactional
  public int saveGradingResults(List<SubmissionGradingResult> results, Attempt attempt) {
    int savedCount = 0;
    int totalScore = 0;

    for (SubmissionGradingResult result : results) {
      if (result.isGraded()) {
        Submission submission =
            submissionRepository
                .findById(result.getSubmissionId())
                .orElseThrow(() -> ResourceNotFoundException.submission(result.getSubmissionId()));

        submission.setAiScore(result.getScore());
        submission.setAiFeedback(result.getFeedback());
        submission.setAiGraded(true);

        submissionRepository.save(submission);
        savedCount++;
        totalScore += submission.getAiScore();

        log.info(
            "Updated submission {} with AI grading results: score={}, feedback={}",
            submission.getId(),
            result.getScore(),
            result.getFeedback());
      }
    }

    attempt.setAiScore(totalScore);
    attempt.setStatus(AttemptStatus.AI_REVIEWED);
    attemptRepository.save(attempt);

    return savedCount;
  }
}
