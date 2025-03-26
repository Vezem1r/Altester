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

    @Transactional
    public void toggleTeacherEditPermission(Long testId, Principal principal) {
        try {
            log.info("User {} is attempting to toggle teacher edit permission for test with ID {}", principal.getName(), testId);

            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> {
                        log.error("User {} not found", principal.getName());
                        return new UserNotFoundException("User not found");
                    });

            if (currentUser.getRole() != RolesEnum.ADMIN) {
                log.warn("User {} is not an admin, access denied", currentUser.getUsername());
                throw new NotAdminException("Only admins can modify teacher edit permissions");
            }

            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> {
                        log.error("Test with ID {} not found", testId);
                        return new TestNotFoundException("Test not found");
                    });

            boolean newState = !test.isAllowTeacherEdit();
            test.setAllowTeacherEdit(newState);
            testRepository.save(test);

            log.info("Teacher edit permission for test ID {} toggled to {} by admin {}", testId, newState, currentUser.getUsername());
        } catch (Exception e) {
            log.error("Error occurred while toggling teacher edit permission for test ID {}", testId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getAllTestsForAdmin(Pageable pageable, Principal principal, String searchQuery, Boolean isActive) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (currentUser.getRole() != RolesEnum.ADMIN) {
                throw new NotAdminException("Only admin can access all tests");
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
        } catch (Exception e) {
            log.error("Error occurred while retrieving all tests for admin", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTeacherTests(Pageable pageable, Principal principal, String searchQuery, Boolean isActive) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (currentUser.getRole() != RolesEnum.TEACHER) {
                throw new TestAccessDeniedException("Only teachers can access this endpoint");
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
        } catch (Exception e) {
            log.error("Error occurred while retrieving teacher's tests", e);
            throw e;
        }
    }

    @Transactional
    public TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Test test = new Test();
            test.setTitle(createTestDTO.getTitle());
            test.setDescription(createTestDTO.getDescription());
            test.setDuration(createTestDTO.getDuration());
            test.setOpen(createTestDTO.isOpen());
            test.setMaxAttempts(createTestDTO.getMaxAttempts());
            test.setStartTime(createTestDTO.getStartTime());
            test.setEndTime(createTestDTO.getEndTime());

            List<Group> selectedGroups = new ArrayList<>();
            List<Group> invalidGroups = new ArrayList<>();

            if (currentUser.getRole() == RolesEnum.ADMIN) {
                test.setCreatedByAdmin(true);

                if (createTestDTO.getSubjectId() != null) {
                    Subject subject = subjectRepository.findById(createTestDTO.getSubjectId())
                            .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

                    if (subject.getGroups() != null && !subject.getGroups().isEmpty()) {
                        for (Group group : subject.getGroups()) {
                            if (groupActivityService.canModifyGroup(group)) {
                                selectedGroups.add(group);
                            } else {
                                invalidGroups.add(group);
                            }
                        }
                    }
                } else if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                    for (Long groupId : createTestDTO.getGroupIds()) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

                        if (groupActivityService.canModifyGroup(group)) {
                            selectedGroups.add(group);
                        } else {
                            invalidGroups.add(group);
                        }
                    }
                }
            } else if (currentUser.getRole() == RolesEnum.TEACHER) {
                test.setCreatedByAdmin(false);

                if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                    List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                    for (Long groupId : createTestDTO.getGroupIds()) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

                        if (teacherGroups.contains(group)) {
                            if (groupActivityService.canModifyGroup(group)) {
                                selectedGroups.add(group);
                            } else {
                                invalidGroups.add(group);
                            }
                        }
                    }
                }
            }

            if (!invalidGroups.isEmpty()) {
                String invalidGroupNames = invalidGroups.stream()
                        .map(Group::getName)
                        .collect(Collectors.joining(", "));
                throw new InvalidGroupSelectionException("Cannot add test to past semester groups: " + invalidGroupNames);
            }

            if (selectedGroups.isEmpty()) {
                throw new InvalidGroupSelectionException("No valid groups selected");
            }

            Test savedTest = testRepository.save(test);

            for (Group group : selectedGroups) {
                group.getTests().add(savedTest);
                groupRepository.save(group);
            }

            return testDTOMapper.convertToTestPreviewDTO(savedTest, currentUser);
        } catch (Exception e) {
            log.error("Error occurred while creating test", e);
            throw e;
        }
    }

    @Transactional
    public TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Test existingTest = testRepository.findById(testId)
                    .orElseThrow(() -> new TestNotFoundException("Test not found"));

            if (currentUser.getRole() == RolesEnum.TEACHER) {
                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                testAccessValidator.canTeacherEditTest(currentUser, existingTest, teacherGroups);
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

                List<Group> newSelectedGroups = new ArrayList<>();
                List<Group> invalidGroups = new ArrayList<>();

                if (currentUser.getRole() == RolesEnum.ADMIN) {
                    if (updateTestDTO.getSubjectId() != null) {
                        Subject subject = subjectRepository.findById(updateTestDTO.getSubjectId())
                                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

                        for (Group group : subject.getGroups()) {
                            if (groupActivityService.canModifyGroup(group)) {
                                newSelectedGroups.add(group);
                            } else {
                                invalidGroups.add(group);
                            }
                        }
                    } else if (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty()) {
                        for (Long groupId : updateTestDTO.getGroupIds()) {
                            Group group = groupRepository.findById(groupId)
                                    .orElseThrow(() -> new GroupNotFoundException("Group not found"));

                            if (groupActivityService.canModifyGroup(group)) {
                                newSelectedGroups.add(group);
                            } else {
                                invalidGroups.add(group);
                            }
                        }
                    }
                } else if (currentUser.getRole() == RolesEnum.TEACHER) {
                    if (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty()) {
                        List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                        for (Long groupId : updateTestDTO.getGroupIds()) {
                            Group group = groupRepository.findById(groupId)
                                    .orElseThrow(() -> new GroupNotFoundException("Group not found"));

                            if (teacherGroups.contains(group)) {
                                if (groupActivityService.canModifyGroup(group)) {
                                    newSelectedGroups.add(group);
                                } else {
                                    invalidGroups.add(group);
                                }
                            }
                        }
                    }
                }

                if (!invalidGroups.isEmpty()) {
                    String invalidGroupNames = invalidGroups.stream()
                            .map(Group::getName)
                            .collect(Collectors.joining(", "));
                    throw new GroupInactiveException("Cannot add test to past semester groups: " + invalidGroupNames);
                }

                if (newSelectedGroups.isEmpty()) {
                    throw new InvalidGroupSelectionException("No valid groups selected");
                }

                List<Group> currentGroups = testDTOMapper.findGroupsByTest(existingTest);
                for (Group group : currentGroups) {
                    group.getTests().remove(existingTest);
                    groupRepository.save(group);
                }

                for (Group group : newSelectedGroups) {
                    group.getTests().add(existingTest);
                    groupRepository.save(group);
                }
            }

            Test updatedTest = testRepository.save(existingTest);
            return testDTOMapper.convertToTestPreviewDTO(updatedTest, currentUser);
        } catch (Exception e) {
            log.error("Error occurred while updating test", e);
            throw e;
        }
    }

    @Transactional
    public void deleteTest(Long testId, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Test existingTest = testRepository.findById(testId)
                    .orElseThrow(() -> new TestNotFoundException("Test not found"));

            if (currentUser.getRole() == RolesEnum.ADMIN) {
                List<Group> testGroups = testDTOMapper.findGroupsByTest(existingTest);
                for (Group group : testGroups) {
                    group.getTests().remove(existingTest);
                    groupRepository.save(group);
                }

                testRepository.delete(existingTest);
            } else if (currentUser.getRole() == RolesEnum.TEACHER) {
                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                if (existingTest.isCreatedByAdmin()) {
                    throw new NotAdminException("Cannot delete admin-created tests");
                }

                testAccessValidator.isTeacherTestCreator(currentUser, existingTest, teacherGroups);

                List<Group> testGroups = testDTOMapper.findGroupsByTest(existingTest);
                for (Group group : testGroups) {
                    group.getTests().remove(existingTest);
                    groupRepository.save(group);
                }

                testRepository.delete(existingTest);
            } else {
                throw new TestAccessDeniedException("Not authorized to delete this test");
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting test", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public TestSummaryDTO getTestSummary(Long testId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException("Test not found"));

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

    @Transactional(readOnly = true)
    public TestPreviewDTO getTestPreview(Long testId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException("Test not found"));

        testAccessValidator.validateTestAccess(currentUser, test);

        return testDTOMapper.convertToTestPreviewDTO(test, currentUser);
    }

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTestsBySubject(Long subjectId, Principal principal, String searchQuery,
                                                  Boolean isActive, Pageable pageable) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

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

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTestsByGroup(Long groupId, Principal principal, String searchQuery,
                                                Boolean isActive, Pageable pageable) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

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

    @Transactional
    public void toggleTestActivity(Long testId, Principal principal) {
        try {
            log.info("User {} is attempting to toggle activity for test with ID {}", principal.getName(), testId);

            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> {
                        log.error("User {} not found", principal.getName());
                        return new UserNotFoundException("User not found");
                    });

            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> {
                        log.error("Test with ID {} not found", testId);
                        return new TestNotFoundException("Test not found");
                    });

            if (currentUser.getRole() != RolesEnum.ADMIN) {
                log.info("User {} is not an admin, checking permissions...", currentUser.getUsername());

                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                testAccessValidator.canTeacherEditTest(currentUser, test, teacherGroups);
            }

            boolean newState = !test.isOpen();
            test.setOpen(newState);
            testRepository.save(test);

            log.info("Test ID {} activity toggled to {} by user {}", testId, newState, currentUser.getUsername());
        } catch (UserNotFoundException | TestNotFoundException ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }
}