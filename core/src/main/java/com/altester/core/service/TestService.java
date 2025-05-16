package com.altester.core.service;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.exception.*;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TestService {

  /**
   * Toggles whether teachers can edit a specific test. Only administrators can perform this action.
   *
   * @param testId ID of the test to modify permissions for
   * @param principal The authenticated administrator
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user is not an administrator
   */
  void toggleTeacherEditPermission(Long testId, Principal principal);

  /**
   * Toggles AI evaluation for a specific test and group combination. Requires an API key to be
   * already assigned to the test-group pair.
   *
   * @param testId ID of the test to toggle AI evaluation for
   * @param groupId ID of the group for which to toggle AI evaluation
   * @param principal The authenticated user (admin or teacher)
   * @throws ResourceNotFoundException if the test, group, or assignment doesn't exist
   * @throws AccessDeniedException if the user doesn't have permission to modify the test or group
   * @throws StateConflictException if there's no API key assigned to the test-group combination
   */
  void toggleAiEvaluation(Long testId, Long groupId, Principal principal);

  /**
   * Retrieves a paginated and searchable list of all tests for administrators, with optional
   * filtering by active status.
   *
   * @param pageable Pagination information
   * @param principal The authenticated administrator
   * @param searchQuery Optional search text to filter tests by title
   * @param isActive Optional filter by test activity status (open/closed)
   * @return Paginated list of tests with group information
   * @throws AccessDeniedException if the user is not an administrator
   */
  CacheablePage<TestSummaryDTO> getAllTestsForAdmin(
      Pageable pageable, Principal principal, String searchQuery, Boolean isActive);

  /**
   * Retrieves a paginated and searchable list of tests for a teacher, with optional filtering by
   * active status and edit permission. Only includes tests associated with groups where the teacher
   * is assigned.
   *
   * @param pageable Pagination information
   * @param principal The authenticated teacher
   * @param searchQuery Optional search text to filter tests by title
   * @param isActive Optional filter by test activity status (open/closed)
   * @param allowTeacherEdit Optional filter for tests that allow teacher editing
   * @return Paginated list of tests with group information
   * @throws AccessDeniedException if the user is not a teacher
   */
  CacheablePage<TestSummaryDTO> getTeacherTests(
      Pageable pageable,
      Principal principal,
      String searchQuery,
      Boolean isActive,
      Boolean allowTeacherEdit);

  /**
   * Creates a new test with the provided details and associates it with the selected groups.
   * Administrators can create tests for any group, while teachers can only create tests for their
   * assigned groups. Automatically assigns a global API key if available.
   *
   * @param createTestDTO DTO containing test details and group associations
   * @param principal The authenticated user (admin or teacher)
   * @return Preview of the created test
   * @throws ResourceNotFoundException if groups don't exist
   * @throws AccessDeniedException if the teacher doesn't have access to selected groups
   */
  TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal);

  /**
   * Updates an existing test with the provided details. Administrators can update any test, while
   * teachers can only update tests they are associated with and that allow teacher editing.
   *
   * @param updateTestDTO DTO containing updated test details
   * @param testId ID of the test to update
   * @param principal The authenticated user (admin or teacher)
   * @return Preview of the updated test
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have permission to edit the test
   */
  TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal);

  /**
   * Deletes a test and removes all its associations with groups. Administrators can delete any
   * test, while teachers can only delete tests they created (not admin-created tests).
   *
   * @param testId ID of the test to delete
   * @param principal The authenticated user (admin or teacher)
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have permission to delete the test
   */
  void deleteTest(Long testId, Principal principal);

  /**
   * Retrieves summary information for a specific test.
   *
   * @param testId ID of the test to retrieve
   * @param principal The authenticated user
   * @return Test summary with associated groups
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have access to the test
   */
  TestSummaryDTO getTestSummary(Long testId, Principal principal);

  /**
   * Retrieves detailed preview information for a specific test, including questions and
   * configuration.
   *
   * @param testId ID of the test to retrieve
   * @param principal The authenticated user
   * @return Detailed test preview
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have access to the test
   */
  TestPreviewDTO getTestPreview(Long testId, Principal principal);

  /**
   * Retrieves a paginated and searchable list of tests associated with a specific subject. For
   * teachers, only includes tests in groups they teach.
   *
   * @param subjectId ID of the subject
   * @param principal The authenticated user
   * @param searchQuery Optional search text to filter tests by title
   * @param isActive Optional filter by test activity status
   * @param pageable Pagination information
   * @return Paginated list of tests with group information
   * @throws ResourceNotFoundException if the subject doesn't exist
   */
  CacheablePage<TestSummaryDTO> getTestsBySubject(
      Long subjectId, Principal principal, String searchQuery, Boolean isActive, Pageable pageable);

  /**
   * Retrieves a paginated and searchable list of tests associated with a specific group.
   *
   * @param groupId ID of the group
   * @param principal The authenticated user
   * @param searchQuery Optional search text to filter tests by title
   * @param isActive Optional filter by test activity status
   * @param pageable Pagination information
   * @return Paginated list of tests with group information
   * @throws ResourceNotFoundException if the group doesn't exist
   * @throws AccessDeniedException if the user doesn't have access to the group
   */
  CacheablePage<TestSummaryDTO> getTestsByGroup(
      Long groupId, Principal principal, String searchQuery, Boolean isActive, Pageable pageable);

  /**
   * Toggles the activity state of a test (open/closed). Administrators can toggle any test, while
   * teachers can only toggle tests they are permitted to edit. When a test is opened, notifications
   * are sent to all students in associated groups.
   *
   * @param testId ID of the test to toggle
   * @param principal The authenticated user (admin or teacher)
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have permission to toggle the test
   */
  void toggleTestActivity(Long testId, Principal principal);

  /**
   * Retrieves a paginated list of questions for a specific test, with optional filtering by
   * difficulty level. This method allows administrators and teachers to view all questions
   * associated with a test.
   *
   * @param testId ID of the test to retrieve questions for
   * @param principal The authenticated user
   * @param pageable Pagination information
   * @param difficulty Optional filter to show only questions of a specific difficulty level
   * @return Paginated list of questions for the test
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have access to the test
   */
  Page<QuestionDTO> getTestQuestions(
      Long testId, Principal principal, Pageable pageable, QuestionDifficulty difficulty);

  /**
   * Generates a randomized test preview for students with the configured number of questions at
   * each difficulty level. The questions are randomly selected based on the test configuration and
   * shuffled.
   *
   * @param testId ID of the test to generate a preview for
   * @param principal The authenticated student
   * @param pageable Pagination information
   * @return Paginated list of randomly selected questions for the student's test attempt
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the user doesn't have access to the test
   */
  Page<QuestionDTO> getStudentTestPreview(Long testId, Principal principal, Pageable pageable);
}
