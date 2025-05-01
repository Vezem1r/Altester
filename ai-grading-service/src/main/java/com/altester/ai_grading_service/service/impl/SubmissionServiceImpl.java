package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.dto.SubmissionGradingResult;
import com.altester.ai_grading_service.exception.ResourceNotFoundException;
import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.repository.SubmissionRepository;
import com.altester.ai_grading_service.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Override
    public List<Submission> getSubmissionsForAttempt(Attempt attempt) {
        return submissionRepository.findByAttempt(attempt);
    }

    @Override
    public List<Submission> getSubmissionsForAiGrading(Attempt attempt) {
        return submissionRepository.findByAttemptAndAiGraded(attempt, false);
    }

    @Override
    @Transactional
    public Submission updateSubmissionWithGradingResults(Submission submission, Integer score, String feedback) {
        submission.setScore(score);
        submission.setTeacherFeedback(feedback);
        submission.setAiGraded(true);
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public int saveGradingResults(List<SubmissionGradingResult> results) {
        int savedCount = 0;

        for (SubmissionGradingResult result : results) {
            if (result.isGraded()) {
                Submission submission = submissionRepository.findById(result.getSubmissionId())
                        .orElseThrow(() -> ResourceNotFoundException.submission(result.getSubmissionId()));

                submission.setScore(result.getScore());
                submission.setTeacherFeedback(result.getFeedback());
                submission.setAiGraded(true);

                submissionRepository.save(submission);
                savedCount++;

                log.info("Updated submission {} with AI grading results: score={}, feedback={}",
                        submission.getId(), result.getScore(), result.getFeedback());
            }
        }
        return savedCount;
    }
}
