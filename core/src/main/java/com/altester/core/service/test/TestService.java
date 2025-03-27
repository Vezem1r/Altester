package com.altester.core.service.test;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.subject.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TestDTOMapper testDTOMapper;
    private final TestAccessValidator testAccessValidator;
    private final GroupActivityService groupActivityService;

    /**
     * Retrieves the current authenticated user.
     *
     * @param principal The authenticated user principal
     * @return User entity for the authenticated user
     * @throws ResourceNotFoundException If the user is not found
     */
    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return ResourceNotFoundException.user(principal.getName());
                });
    }

    /**
     * Retrieves a test by its ID.
     *
     * @param testId The ID of the test to retrieve
     * @return Test entity
     * @throws ResourceNotFoundException If the test is not found
     */
    private Test getTestById(Long testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.error("Test with ID {} not found", testId);
                    return ResourceNotFoundException.test(testId);
                });
    }

    /**
     * Identifies and returns valid groups for test assignment based on user role and permissions.
     * Administrators can assign tests to any active group or by subject.
     * Teachers can only assign tests to their own active groups.
     * Inactive groups are silently skipped when using subjectId.
     *
     * @param currentUser The current authenticated user
     * @param createTestDTO DTO containing group or subject selection criteria
     * @return List of valid Group entities for test assignment
     * @throws ResourceNotFoundException If any specified group or subject is not found
     * @throws StateConflictException If any explicitly selected groups (via groupIds) are inactive
     * @throws ValidationException If no valid groups are identified
     */
    private List<Group> findValidGroupsForTest(User currentUser, CreateTestDTO createTestDTO) {
        List<Group> selectedGroups = new ArrayList<>();
        List<Group> invalidGroups = new ArrayList<>();

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            if (createTestDTO.getSubjectId() != null) {
                Subject subject = subjectRepository.findById(createTestDTO.getSubjectId())
                        .orElseThrow(() -> ResourceNotFoundException.subject(createTestDTO.getSubjectId()));

                if (subject.getGroups() != null && !subject.getGroups().isEmpty()) {
                    for (Group group : subject.getGroups()) {
                        if (groupActivityService.canModifyGroup(group)) {
                            selectedGroups.add(group);
                        } else {
                            log.warn("Skipping inactive group {} from subject {}", group.getName(), subject.getShortName());
                        }
                    }
                }
            } else if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> ResourceNotFoundException.group(groupId));

                    if (groupActivityService.canModifyGroup(group)) {
                        selectedGroups.add(group);
                    } else {
                        invalidGroups.add(group);
                    }
                }

                if (!invalidGroups.isEmpty()) {
                    String invalidGroupNames = invalidGroups.stream()
                            .map(Group::getName)
                            .collect(Collectors.joining(", "));
                    throw StateConflictException.inactiveGroup(invalidGroupNames);
                }
            }
        } else if (currentUser.getRole() == RolesEnum.TEACHER) {
            if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> ResourceNotFoundException.group(groupId));

                    if (teacherGroups.contains(group)) {
                        if (groupActivityService.canModifyGroup(group)) {
                            selectedGroups.add(group);
                        } else {
                            invalidGroups.add(group);
                        }
                    }
                }

                if (!invalidGroups.isEmpty()) {
                    String invalidGroupNames = invalidGroups.stream()
                            .map(Group::getName)
                            .collect(Collectors.joining(", "));
                    throw StateConflictException.inactiveGroup(invalidGroupNames);
                }
            }
        }

        if (selectedGroups.isEmpty()) {
            throw ValidationException.groupValidation("No valid groups selected for test assignment");
        }

        return selectedGroups;
    }

    /**
     * Toggles whether teachers can edit a specific test.
     * Only administrators can perform this action.
     *
     * @param testId The ID of the test for which to toggle the edit permission
     * @param principal The authenticated user principal
     * @throws ResourceNotFoundException If the test is not found
     * @throws AccessDeniedException If the current user is not an administrator
     */
    @Transactional
    public void toggleTeacherEditPermission(Long testId, Principal principal) {
        log.info("User {} is attempting to toggle teacher edit permission for test with ID {}", principal.getName(), testId);

        User currentUser = getCurrentUser(principal);

        if (currentUser.getRole() != RolesEnum.ADMIN) {
            log.warn("User {} is not an admin, access denied", currentUser.getUsername());
            throw AccessDeniedException.notAdmin();
        }

        Test test = getTestById(testId);
        boolean newState = !test.isAllowTeacherEdit();
        test.setAllowTeacherEdit(newState);
        testRepository.save(test);

        log.info("Teacher edit permission for test ID {} toggled to {} by admin {}", testId, newState, currentUser.getUsername());
    }

    /**
     * Retrieves a paginated list of all tests with filtering options for administrators.
     *
     * @param pageable Pagination information
     * @param principal The authenticated user principal
     * @param searchQuery Optional search query to filter tests by title or description
     * @param isActive Optional filter to show only active or inactive tests
     * @return Page of TestSummaryDTO objects with associated groups
     * @throws ResourceNotFoundException If the user is not found
     * @throws AccessDeniedException If the current user is not an administrator
     */
    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getAllTestsForAdmin(Pageable pageable, Principal principal, String searchQuery, Boolean isActive) {
        log.debug("Getting all tests for admin with search query: {}, isActive: {}", searchQuery, isActive);

        User currentUser = getCurrentUser(principal);

        if (currentUser.getRole() != RolesEnum.ADMIN) {
            throw AccessDeniedException.notAdmin();
        }

        Page<Test> testsPage = testRepository.findAllWithFilters(searchQuery, isActive, pageable);

        return testsPage.map(test -> {
            TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

            List<Group> testGroups = testDTOMapper.findGroupsByTest(test);

            List<GroupSummaryDTO> groupDTOs = testGroups.stream()
                    .map(group -> GroupSummaryDTO.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build())
                    .collect(Collectors.toList());

            dto.setAssociatedGroups(groupDTOs);
            return dto;
        });
    }

    /**
     * Retrieves a paginated list of tests assigned to groups associated with the authenticated teacher.
     *
     * @param pageable Pagination information
     * @param principal The authenticated user principal
     * @param searchQuery Optional search query to filter tests by title or description
     * @param isActive Optional filter to show only active or inactive tests
     * @return Page of TestSummaryDTO objects with associated groups
     * @throws ResourceNotFoundException If the user is not found
     * @throws AccessDeniedException If the current user is not a teacher
     */
    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTeacherTests(Pageable pageable, Principal principal, String searchQuery, Boolean isActive) {
        log.debug("Getting tests for teacher with search query: {}, isActive: {}", searchQuery, isActive);

        User currentUser = getCurrentUser(principal);

        if (currentUser.getRole() != RolesEnum.TEACHER) {
            throw AccessDeniedException.roleConflict();
        }

        Page<Test> testsPage = testRepository.findByTeacherWithFilters(
                currentUser.getId(), searchQuery, isActive, pageable);

        return testsPage.map(test -> {
            TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            List<Group> testGroups = testDTOMapper.findGroupsByTest(test).stream()
                    .filter(teacherGroups::contains)
                    .toList();

            List<GroupSummaryDTO> groupDTOs = testGroups.stream()
                    .map(group -> GroupSummaryDTO.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build())
                    .collect(Collectors.toList());

            dto.setAssociatedGroups(groupDTOs);
            return dto;
        });
    }

    /**
     * Creates a new test with the provided details and associates it with the selected groups.
     * Administrators can create tests for any group, while teachers can only create tests for their assigned groups.
     *
     * @param createTestDTO The data transfer object containing test creation information
     * @param principal The authenticated user principal
     * @return TestPreviewDTO containing the created test details
     * @throws ResourceNotFoundException If the user, group, or subject is not found
     * @throws ValidationException If no valid groups are selected
     * @throws StateConflictException If trying to add test to inactive groups
     */
    @Transactional
    public TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal) {
        log.info("Creating new test with title: {}", createTestDTO.getTitle());

        User currentUser = getCurrentUser(principal);

        Test test = new Test();
        test.setTitle(createTestDTO.getTitle());
        test.setDescription(createTestDTO.getDescription());
        test.setDuration(createTestDTO.getDuration());
        test.setOpen(createTestDTO.isOpen());
        test.setMaxAttempts(createTestDTO.getMaxAttempts());
        test.setStartTime(createTestDTO.getStartTime());
        test.setEndTime(createTestDTO.getEndTime());
        test.setCreatedByAdmin(currentUser.getRole() == RolesEnum.ADMIN);

        List<Group> selectedGroups = findValidGroupsForTest(currentUser, createTestDTO);

        Test savedTest = testRepository.save(test);

        selectedGroups.forEach(group -> group.getTests().add(savedTest));
        groupRepository.saveAll(selectedGroups);

        return testDTOMapper.convertToTestPreviewDTO(savedTest, currentUser);
    }

    /**
     * Updates an existing test with the provided details.
     * Administrators can update any test, while teachers can only update tests they are associated with
     * and that allow teacher editing.
     *
     * @param updateTestDTO The data transfer object containing test update information
     * @param testId The ID of the test to update
     * @param principal The authenticated user principal
     * @return TestPreviewDTO containing the updated test details
     * @throws ResourceNotFoundException If the user, test, group, or subject is not found
     * @throws AccessDeniedException If the teacher cannot edit the test
     * @throws ValidationException If no valid groups are selected when changing group associations
     * @throws StateConflictException If trying to add test to inactive groups
     */
    @Transactional
    public TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal) {
        log.info("Updating test with ID: {}", testId);

        User currentUser = getCurrentUser(principal);
        Test existingTest = getTestById(testId);

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            testAccessValidator.validateTeacherEditAccess(currentUser, existingTest, teacherGroups);
        }

        if (updateTestDTO.getTitle() != null) {
            existingTest.setTitle(updateTestDTO.getTitle());
        }

        if (updateTestDTO.getDescription() != null) {
            existingTest.setDescription(updateTestDTO.getDescription());
        }

        if (updateTestDTO.getDuration() > 0) {
            existingTest.setDuration(updateTestDTO.getDuration());
        }

        existingTest.setOpen(updateTestDTO.isOpen());

        if (updateTestDTO.getMaxAttempts() != null) {
            existingTest.setMaxAttempts(updateTestDTO.getMaxAttempts());
        }

        if (updateTestDTO.getStartTime() != null) {
            existingTest.setStartTime(updateTestDTO.getStartTime());
        }

        if (updateTestDTO.getEndTime() != null) {
            existingTest.setEndTime(updateTestDTO.getEndTime());
        }

        if (updateTestDTO.getSubjectId() != null ||
                (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty())) {

            List<Group> newSelectedGroups = findValidGroupsForTest(currentUser, updateTestDTO);

            List<Group> currentGroups = testDTOMapper.findGroupsByTest(existingTest);
            currentGroups.forEach(group -> group.getTests().remove(existingTest));
            groupRepository.saveAll(currentGroups);

            newSelectedGroups.forEach(group -> group.getTests().add(existingTest));
            groupRepository.saveAll(newSelectedGroups);
        }

        Test updatedTest = testRepository.save(existingTest);
        return testDTOMapper.convertToTestPreviewDTO(updatedTest, currentUser);
    }

    /**
     * Deletes a test and removes all its associations with groups.
     * Administrators can delete any test, while teachers can only delete tests they created
     * (not admin-created tests).
     *
     * @param testId The ID of the test to delete
     * @param principal The authenticated user principal
     * @throws ResourceNotFoundException If the user or test is not found
     * @throws AccessDeniedException If the current user does not have permission to delete the test
     */
    @Transactional
    public void deleteTest(Long testId, Principal principal) {
        log.info("Deleting test with ID: {}", testId);

        User currentUser = getCurrentUser(principal);
        Test existingTest = getTestById(testId);

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            performTestDeletion(existingTest);
        } else if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

            if (existingTest.isCreatedByAdmin()) {
                throw AccessDeniedException.testEdit();
            }

            if (!testAccessValidator.hasTestGroupAssociation(currentUser, existingTest, teacherGroups)) {
                throw AccessDeniedException.testAccess();
            }

            performTestDeletion(existingTest);
        } else {
            throw AccessDeniedException.testAccess();
        }
    }

    /**
     * Performs the actual test deletion including removing all associations with groups.
     *
     * @param test The test entity to delete
     */
    private void performTestDeletion(Test test) {
        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
        testGroups.forEach(group -> group.getTests().remove(test));
        groupRepository.saveAll(testGroups);

        testRepository.delete(test);
        log.info("Test with ID {} has been deleted", test.getId());
    }

    /**
     * Retrieves a summary of a specific test with its basic details and associated groups.
     * The user must have access to the test to view its summary.
     *
     * @param testId The ID of the test to retrieve
     * @param principal The authenticated user principal
     * @return TestSummaryDTO containing the test details and its associated groups
     * @throws ResourceNotFoundException If the user or test is not found
     * @throws AccessDeniedException If the current user does not have access to the test
     */
    @Transactional(readOnly = true)
    public TestSummaryDTO getTestSummary(Long testId, Principal principal) {
        log.debug("Getting test summary for test ID: {}", testId);

        User currentUser = getCurrentUser(principal);
        Test test = getTestById(testId);

        testAccessValidator.validateTestAccess(currentUser, test);

        TestSummaryDTO summaryDTO = testDTOMapper.convertToTestSummaryDTO(test);

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
        List<GroupSummaryDTO> groupDTOs = new ArrayList<>();

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            for (Group group : testGroups) {
                groupDTOs.add(GroupSummaryDTO.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .build());
            }
        } else if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            for (Group group : testGroups) {
                if (teacherGroups.contains(group)) {
                    groupDTOs.add(GroupSummaryDTO.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build());
                }
            }
        }

        summaryDTO.setAssociatedGroups(groupDTOs);
        return summaryDTO;
    }

    /**
     * Retrieves a detailed preview of a specific test.
     * The user must have access to the test to view its preview.
     *
     * @param testId The ID of the test to preview
     * @param principal The authenticated user principal
     * @return TestPreviewDTO containing the detailed test information
     * @throws ResourceNotFoundException If the user or test is not found
     * @throws AccessDeniedException If the current user does not have access to the test
     */
    @Transactional(readOnly = true)
    public TestPreviewDTO getTestPreview(Long testId, Principal principal) {
        log.debug("Getting test preview for test ID: {}", testId);

        User currentUser = getCurrentUser(principal);
        Test test = getTestById(testId);

        testAccessValidator.validateTestAccess(currentUser, test);

        return testDTOMapper.convertToTestPreviewDTO(test, currentUser);
    }

    /**
     * Retrieves a paginated list of tests associated with a specific subject.
     * Filters tests based on user role permissions.
     *
     * @param subjectId The ID of the subject whose tests to retrieve
     * @param principal The authenticated user principal
     * @param searchQuery Optional search query to filter tests by title or description
     * @param isActive Optional filter to show only active or inactive tests
     * @param pageable Pagination information
     * @return Page of TestSummaryDTO objects with associated groups
     * @throws ResourceNotFoundException If the user or subject is not found
     */
    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTestsBySubject(Long subjectId, Principal principal, String searchQuery,
                                                  Boolean isActive, Pageable pageable) {
        log.debug("Getting tests for subject ID: {} with search query: {}, isActive: {}", subjectId, searchQuery, isActive);

        User currentUser = getCurrentUser(principal);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.subject(subjectId));

        Page<Test> testsPage = testRepository.findBySubjectWithFilters(
                subjectId, searchQuery, isActive, pageable);

        List<Group> teacherGroups = currentUser.getRole() == RolesEnum.TEACHER
                ? groupRepository.findByTeacher(currentUser)
                : Collections.emptyList();

        return testsPage.map(test -> {
            TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

            List<Group> testGroups = testDTOMapper.findGroupsByTest(test).stream()
                    .filter(group -> subject.getGroups().contains(group))
                    .filter(group -> currentUser.getRole() == RolesEnum.ADMIN ||
                            (currentUser.getRole() == RolesEnum.TEACHER && teacherGroups.contains(group)))
                    .toList();

            List<GroupSummaryDTO> groupDTOs = testGroups.stream()
                    .map(group -> GroupSummaryDTO.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build())
                    .collect(Collectors.toList());

            dto.setAssociatedGroups(groupDTOs);
            return dto;
        });
    }
    /**
     * Retrieves a paginated list of tests associated with a specific group.
     * The user must have access to the group to view its tests.
     *
     * @param groupId The ID of the group whose tests to retrieve
     * @param principal The authenticated user principal
     * @param searchQuery Optional search query to filter tests by title or description
     * @param isActive Optional filter to show only active or inactive tests
     * @param pageable Pagination information
     * @return Page of TestSummaryDTO objects with the specified group
     * @throws ResourceNotFoundException If the user or group is not found
     * @throws AccessDeniedException If the current user does not have access to the group
     */
    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTestsByGroup(Long groupId, Principal principal, String searchQuery,
                                                Boolean isActive, Pageable pageable) {
        log.debug("Getting tests for group ID: {} with search query: {}, isActive: {}", groupId, searchQuery, isActive);

        User currentUser = getCurrentUser(principal);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> ResourceNotFoundException.group(groupId));

        testAccessValidator.validateGroupAccess(currentUser, group);

        Page<Test> testsPage = testRepository.findByGroupWithFilters(
                groupId, searchQuery, isActive, pageable);

        return testsPage.map(test -> {
            TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

            List<GroupSummaryDTO> groupDTOs = new ArrayList<>();
            groupDTOs.add(GroupSummaryDTO.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .build());

            dto.setAssociatedGroups(groupDTOs);
            return dto;
        });
    }
    /**
     * Toggles the activity state of a test (open/closed).
     * Administrators can toggle any test, while teachers can only toggle tests they are permitted to edit.
     *
     * @param testId The ID of the test to toggle
     * @param principal The authenticated user principal
     * @throws ResourceNotFoundException If the user or test is not found
     * @throws AccessDeniedException If the current user does not have permission to edit the test
     */
    @Transactional
    public void toggleTestActivity(Long testId, Principal principal) {
        log.info("User {} is attempting to toggle activity for test with ID {}", principal.getName(), testId);

        User currentUser = getCurrentUser(principal);
        Test test = getTestById(testId);

        if (currentUser.getRole() != RolesEnum.ADMIN) {
            log.info("User {} is not an admin, checking permissions...", currentUser.getUsername());

            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            testAccessValidator.validateTeacherEditAccess(currentUser, test, teacherGroups);
        }

        boolean newState = !test.isOpen();
        test.setOpen(newState);
        testRepository.save(test);

        log.info("Test ID {} activity toggled to {} by user {}", testId, newState, currentUser.getUsername());
    }
}