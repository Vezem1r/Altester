package com.altester.core.service;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.dtos.core_service.review.AttemptReviewSubmissionDTO;
import com.altester.core.dtos.core_service.student.AttemptReviewDTO;

import java.security.Principal;
import java.util.List;

public interface AttemptRetrievalService {

    List<TestAttemptsForGroupDTO> getTestAttemptsForTeacher(
            Principal principal, Long testId, String searchQuery);

    List<TestAttemptsForGroupDTO> getTestAttemptsForAdmin(
            Principal principal, Long testId, String searchQuery);

    StudentTestAttemptsResponseDTO getStudentAttemptsForTeacher(
            Principal principal, String username, String searchQuery);

    StudentTestAttemptsResponseDTO getStudentAttemptsForAdmin(
            Principal principal, String username, String searchQuery);

    AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId);

    void submitAttemptReview(
            Principal principal, Long attemptId, AttemptReviewSubmissionDTO reviewSubmission);
}