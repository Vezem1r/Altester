package com.altester.ai_grading_service.service;

import com.altester.ai_grading_service.dto.SubmissionGradingResult;
import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;

import java.util.List;

public interface SubmissionService {

    /**
     * Get all submissions for an attempt that need AI grading
     *
     * @param attempt The test attempt
     * @return List of submissions requiring AI grading
     */
    List<Submission> getSubmissionsForAiGrading(Attempt attempt);

    /**
     * Save multiple grading results
     *
     * @param results List of grading results to save
     * @return Number of results saved
     */
    int saveGradingResults(List<SubmissionGradingResult> results, Attempt attempt);
}
