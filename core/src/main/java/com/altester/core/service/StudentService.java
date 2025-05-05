package com.altester.core.service;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.exception.*;

import java.security.Principal;

public interface StudentService {

    /**
     * Retrieves the student dashboard containing current active groups and their tests,
     * with optional filtering by search query and/or specific group.
     *
     * @param principal The authenticated student
     * @param searchQuery Optional search text to filter test names
     * @param groupId Optional ID to filter results for a specific group
     * @return StudentDashboardResponse containing student information and current groups with tests
     * @throws ResourceNotFoundException if the student is not found
     * @throws AccessDeniedException if the student attempts to access a group they are not a member of
     */
    StudentDashboardResponse getStudentDashboard(Principal principal, String searchQuery, Long groupId);

    /**
     * Retrieves the student's academic history grouped by academic periods (semester and year),
     * with optional filtering by specific period and/or search query.
     * Only includes past (inactive) groups and their associated tests.
     *
     * @param principal The authenticated student
     * @param academicYear Optional academic year to filter results
     * @param semester Optional semester to filter results
     * @param searchQuery Optional search text to filter test names
     * @return AcademicHistoryResponse containing past groups and tests grouped by academic periods
     * @throws ResourceNotFoundException if the student is not found
     */
    AcademicHistoryResponse getAcademicHistory(Principal principal, Integer academicYear, Semester semester, String searchQuery);

    /**
     * Retrieves the list of all academic periods (semester and year combinations)
     * available in the student's academic history, sorted by recency.
     *
     * @param principal The authenticated student
     * @return AvailablePeriodsResponse containing the list of unique academic periods
     * @throws ResourceNotFoundException if the student is not found
     */
    AvailablePeriodsResponse getAvailablePeriods(Principal principal);

    /**
     * Retrieves all completed attempts made by the student for a specific test.
     *
     * @param principal The authenticated student
     * @param testId The ID of the test to retrieve attempts for
     * @return StudentAttemptsResponse containing test information and all attempts
     * @throws ResourceNotFoundException if the student or test is not found
     * @throws StateConflictException if the test is closed
     * @throws AccessDeniedException if the student doesn't have access to the test
     */
    StudentAttemptsResponse getStudentTestAttempts(Principal principal, Long testId);

    /**
     * Retrieves detailed review information for a specific test attempt,
     * including questions, answers, scores, and feedback.
     *
     * @param principal The authenticated student
     * @param attemptId The ID of the attempt to retrieve
     * @return AttemptReviewDTO containing detailed review information
     * @throws ResourceNotFoundException if the student or attempt is not found
     * @throws AccessDeniedException if the student doesn't own the attempt
     * @throws StateConflictException if the attempt is still in progress
     */
    AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId);

    /**
     * Requests re-grading for AI-graded submissions by a student.
     * All submissions must belong to the same attempt owned by the student.
     * Only AI-graded submissions that haven't already been marked for re-grading can be requested.
     *
     * @param principal The authenticated student
     * @param regradeRequest The DTO containing submission IDs to request re-grading for
     * @throws ResourceNotFoundException if the student or any submission is not found
     * @throws AccessDeniedException if the student doesn't own the submissions
     * @throws StateConflictException if submissions are not valid for re-grading
     */
    void requestRegrade(Principal principal, RegradeRequestDTO regradeRequest);
}