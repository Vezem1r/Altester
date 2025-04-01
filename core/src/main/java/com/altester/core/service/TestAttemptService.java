package com.altester.core.service;

import com.altester.core.dtos.core_service.attempt.*;

import java.security.Principal;

public interface TestAttemptService {

    /**
     * Starts a new test attempt or resumes an existing active attempt.
     * @return information about the first question or the question of where the student left off in case of resuming
     */
    SingleQuestionResponse startAttempt(Principal principal, StartAttemptRequest request);
    SingleQuestionResponse getQuestion(Principal principal, GetQuestionRequest request);
    void saveAnswer(Principal principal, SaveAnswerRequest request);

    /**
     * Saves the current answer (if provided) and navigates to the next question in the test.
     * @return information about the next question and the current state of the attempt
     */
    SingleQuestionResponse nextQuestion(Principal principal, NextQuestionRequest request);
    SingleQuestionResponse previousQuestion(Principal principal, PreviousQuestionRequest request);

    AttemptResultResponse completeAttempt(Principal principal, CompleteAttemptRequest request);

    /**
     * Retrieves the current status of a test attempt, including answered questions,
     * time remaining, and other metadata.
     */
    AttemptStatusResponse getAttemptStatus(Principal principal, Long attemptId);
}