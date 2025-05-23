package com.altester.core.service;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.dtos.core_service.student.AttemptReviewDTO;
import com.altester.core.exception.*;
import java.security.Principal;
import java.util.List;

public interface AttemptRetrievalService {

  /**
   * Retrieves test attempts grouped by student groups for a specific test accessible to a teacher.
   * Only includes attempts from groups where the authenticated user is the assigned teacher.
   *
   * @param principal The authenticated teacher
   * @param testId The ID of the test to retrieve attempts for
   * @param searchQuery Optional search text to filter students (can be null)
   * @return List of test attempts grouped by student groups
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the authenticated user is not a teacher
   */
  List<TestAttemptsForGroupDTO> getTestAttemptsForTeacher(
      Principal principal, Long testId, String searchQuery);

  /**
   * Retrieves test attempts grouped by student groups for a specific test, accessible only to
   * administrators. Includes attempts from all groups associated with the test.
   *
   * @param principal The authenticated administrator
   * @param testId The ID of the test to retrieve attempts for
   * @param searchQuery Optional search text to filter students (can be null)
   * @return List of test attempts grouped by student groups
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if the authenticated user is not an administrator
   */
  List<TestAttemptsForGroupDTO> getTestAttemptsForAdmin(
      Principal principal, Long testId, String searchQuery);

  /**
   * Retrieves all test attempts made by a specific student in groups taught by the authenticated
   * teacher. Only includes attempts for groups where the teacher is assigned and the student is a
   * member.
   *
   * @param principal The authenticated teacher
   * @param username The username of the student
   * @param searchQuery Optional search text to filter test names (can be null)
   * @return DTO containing student information and their test attempts
   * @throws ResourceNotFoundException if the student doesn't exist
   * @throws AccessDeniedException if the authenticated user is not a teacher
   */
  StudentTestAttemptsResponseDTO getStudentAttemptsForTeacher(
      Principal principal, String username, String searchQuery);

  /**
   * Retrieves all test attempts made by a specific student, accessible only to administrators.
   * Includes all attempts regardless of group membership.
   *
   * @param principal The authenticated administrator
   * @param username The username of the student
   * @param searchQuery Optional search text to filter test names (can be null)
   * @return DTO containing student information and their test attempts
   * @throws ResourceNotFoundException if the student doesn't exist
   * @throws AccessDeniedException if the authenticated user is not an administrator
   */
  StudentTestAttemptsResponseDTO getStudentAttemptsForAdmin(
      Principal principal, String username, String searchQuery);

  /**
   * Retrieves detailed review information for a specific test attempt. Accessible to teachers who
   * teach the group the student belongs to, and administrators.
   *
   * @param principal The authenticated user (teacher or administrator)
   * @param attemptId The ID of the attempt to retrieve
   * @return DTO containing detailed attempt review information
   * @throws ResourceNotFoundException if the attempt doesn't exist
   * @throws AccessDeniedException if the authenticated user doesn't have permission to access this
   *     attempt
   */
  AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId);

  /**
   * Retrieves detailed information about a student's attempts for a specific test. Accessible to
   * teachers who teach the group the student belongs to.
   *
   * @param principal The authenticated teacher
   * @param testId The ID of the test
   * @param username The username of the student
   * @return List of attempt information
   * @throws ResourceNotFoundException if the test or student doesn't exist
   * @throws AccessDeniedException if the teacher doesn't teach the student's group
   */
  List<AttemptInfoDTO> getStudentTestAttemptsForTeacher(
      Principal principal, Long testId, String username);

  /**
   * Retrieves detailed information about a student's attempts for a specific test. Accessible only
   * to administrators.
   *
   * @param principal The authenticated administrator
   * @param testId The ID of the test
   * @param username The username of the student
   * @return List of attempt information
   * @throws ResourceNotFoundException if the test or student doesn't exist
   * @throws AccessDeniedException if the authenticated user is not an administrator
   */
  List<AttemptInfoDTO> getStudentTestAttemptsForAdmin(
      Principal principal, Long testId, String username);
}
