package com.altester.core.service;

import com.altester.core.dtos.core_service.test.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface TestService {

    /**
     * Toggles whether teachers can edit a specific test.
     * Only administrators can perform this action.
     *
     * @param testId The ID of the test for which to toggle the edit permission
     * @param principal The authenticated user principal
     */
    void toggleTeacherEditPermission(Long testId, Principal principal);

    Page<TestSummaryDTO> getAllTestsForAdmin(Pageable pageable, Principal principal, String searchQuery, Boolean isActive);

    Page<TestSummaryDTO> getTeacherTests(Pageable pageable, Principal principal, String searchQuery, Boolean isActive);

    /**
     * Creates a new test with the provided details and associates it with the selected groups.
     * Administrators can create tests for any group, while teachers can only create tests for their assigned groups.
     */
    TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal);

    /**
     * Updates an existing test with the provided details.
     * Administrators can update any test, while teachers can only update tests they are associated with
     * and that allow teacher editing.
     */
    TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal);

    /**
     * Deletes a test and removes all its associations with groups.
     * Administrators can delete any test, while teachers can only delete tests they created
     * (not admin-created tests).
     */
    void deleteTest(Long testId, Principal principal);

    TestSummaryDTO getTestSummary(Long testId, Principal principal);

    TestPreviewDTO getTestPreview(Long testId, Principal principal);

    Page<TestSummaryDTO> getTestsBySubject(Long subjectId, Principal principal, String searchQuery,
                                           Boolean isActive, Pageable pageable);

    Page<TestSummaryDTO> getTestsByGroup(Long groupId, Principal principal, String searchQuery,
                                         Boolean isActive, Pageable pageable);

    /**
     * Toggles the activity state of a test (open/closed).
     * Administrators can toggle any test, while teachers can only toggle tests they are permitted to edit.
     */
    void toggleTestActivity(Long testId, Principal principal);
}