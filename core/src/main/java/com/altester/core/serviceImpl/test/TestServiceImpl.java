package com.altester.core.serviceImpl.test;

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
import com.altester.core.service.TestService;
import com.altester.core.serviceImpl.group.GroupActivityService;
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
public class TestServiceImpl  implements TestService {
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TestDTOMapper testDTOMapper;
    private final TestAccessValidator testAccessValidator;
    private final GroupActivityService groupActivityService;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return ResourceNotFoundException.user(principal.getName());
                });
    }

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

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTeacherTests(Pageable pageable, Principal principal, String searchQuery,
                                                Boolean isActive, Boolean allowTeacherEdit) {
        log.debug("Getting tests for teacher with searchQuery: '{}', isActive: {}, allowTeacherEdit: {}",
                searchQuery, isActive, allowTeacherEdit);

        User currentUser = getCurrentUser(principal);

        if (currentUser.getRole() != RolesEnum.TEACHER) {
            throw AccessDeniedException.roleConflict();
        }

        Page<Test> testsPage = testRepository.findByTeacherWithFilters(
                currentUser.getId(), searchQuery, isActive, allowTeacherEdit, pageable);

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

    @Override
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
        test.setAllowTeacherEdit(currentUser.getRole() == RolesEnum.TEACHER);
        test.setMaxQuestions(createTestDTO.getMaxQuestions());

        List<Group> selectedGroups = findValidGroupsForTest(currentUser, createTestDTO);

        Test savedTest = testRepository.save(test);

        selectedGroups.forEach(group -> group.getTests().add(savedTest));
        groupRepository.saveAll(selectedGroups);

        return testDTOMapper.convertToTestPreviewDTO(savedTest, currentUser);
    }

    @Override
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

        if (updateTestDTO.getMaxQuestions() != null) {
            existingTest.setMaxQuestions(updateTestDTO.getMaxQuestions());
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

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public TestPreviewDTO getTestPreview(Long testId, Principal principal) {
        log.debug("Getting test preview for test ID: {}", testId);

        User currentUser = getCurrentUser(principal);
        Test test = getTestById(testId);

        testAccessValidator.validateTestAccess(currentUser, test);

        return testDTOMapper.convertToTestPreviewDTO(test, currentUser);
    }

    @Override
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
    @Override
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
    @Override
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