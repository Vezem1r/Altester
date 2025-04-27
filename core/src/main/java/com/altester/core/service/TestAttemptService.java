package com.altester.core.service;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.exception.*;

import java.security.Principal;

public interface TestAttemptService {

    /**
     * Starts a new test attempt or resumes an existing active attempt.
     * If there's an active attempt, continues from where the student left off.
     * If there's no active attempt, creates a new one if the student hasn't exceeded the maximum attempts.
     *
     * @param principal The authenticated student
     * @param request Object containing the test ID to attempt
     * @return Information about the first question or the question where the student left off
     * @throws ResourceNotFoundException if the test or student doesn't exist
     * @throws AccessDeniedException if the student doesn't have access to the test
     * @throws StateConflictException if an existing attempt has expired
     * @throws ValidationException if the test is not available for the student or max attempts reached
     */
    SingleQuestionResponse startAttempt(Principal principal, StartAttemptRequest request);

    /**
     * Retrieves a specific question for a test attempt.
     *
     * @param principal The authenticated student
     * @param request Object containing the attempt ID and question number
     * @return Information about the requested question
     * @throws ResourceNotFoundException if the attempt doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt has expired or is already completed
     * @throws ValidationException if the question number is invalid
     */
    SingleQuestionResponse getQuestion(Principal principal, GetQuestionRequest request);

    /**
     * Saves a student's answer for a specific question in a test attempt
     * without changing the current question.
     *
     * @param principal The authenticated student
     * @param request Object containing the attempt ID and answer data
     * @throws ResourceNotFoundException if the attempt or question doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt has expired or is already completed
     */
    void saveAnswer(Principal principal, SaveAnswerRequest request);

    /**
     * Saves the current answer (if provided) and navigates to the next question in the test.
     *
     * @param principal The authenticated student
     * @param request Object containing the attempt ID, current question number, and optional answer
     * @return Information about the next question and the current state of the attempt
     * @throws ResourceNotFoundException if the attempt doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt has expired or is already completed
     * @throws ValidationException if already at the last question
     */
    SingleQuestionResponse nextQuestion(Principal principal, NextQuestionRequest request);

    /**
     * Saves the current answer (if provided) and navigates to the previous question in the test.
     *
     * @param principal The authenticated student
     * @param request Object containing the attempt ID, current question number, and optional answer
     * @return Information about the previous question and the current state of the attempt
     * @throws ResourceNotFoundException if the attempt doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt has expired or is already completed
     * @throws ValidationException if already at the first question
     */
    SingleQuestionResponse previousQuestion(Principal principal, PreviousQuestionRequest request);

    /**
     * Completes a test attempt, calculates the score, and returns the result.
     * Automatically grades multiple-choice questions.
     *
     * @param principal The authenticated student
     * @param request Object containing the attempt ID to complete
     * @return Result information including score and statistics
     * @throws ResourceNotFoundException if the attempt doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt is already completed
     */
    AttemptResultResponse completeAttempt(Principal principal, CompleteAttemptRequest request);

    /**
     * Retrieves the current status of a test attempt, including answered questions,
     * time remaining, and other metadata.
     * Will auto-complete the attempt if it has expired.
     *
     * @param principal The authenticated student
     * @param attemptId ID of the attempt to check status
     * @return Current status of the attempt including question status and time remaining
     * @throws ResourceNotFoundException if the attempt doesn't exist
     * @throws AccessDeniedException if the student doesn't own the attempt
     */
    AttemptStatusResponse getAttemptStatus(Principal principal, Long attemptId);
}