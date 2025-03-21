package com.altester.core.service.test;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getAllTestsForAdmin(Pageable pageable, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != RolesEnum.ADMIN) {
            throw new RuntimeException("Only admin can access all tests");
        }

        Page<Test> testsPage = testRepository.findAll(pageable);

        List<TestSummaryDTO> testSummaries = testsPage.getContent().stream()
                .map(test -> {
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
                })
                .collect(Collectors.toList());

        return new PageImpl<>(testSummaries, pageable, testsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<TestSummaryDTO> getTeacherTests(Pageable pageable, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != RolesEnum.TEACHER) {
            throw new RuntimeException("Only teachers can access this endpoint");
        }

        List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
        Set<Test> teacherTests = new HashSet<>();

        for (Group group : teacherGroups) {
            teacherTests.addAll(group.getTests());
        }

        List<Test> testsList = new ArrayList<>(teacherTests);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), testsList.size());

        if (start >= testsList.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, testsList.size());
        }

        List<TestSummaryDTO> resultList = testsList.subList(start, end).stream()
                .map(test -> {
                    TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

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
                })
                .collect(Collectors.toList());

        return new PageImpl<>(resultList, pageable, testsList.size());
    }

    @Transactional
    public TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Test test = new Test();
        test.setTitle(createTestDTO.getTitle());
        test.setDescription(createTestDTO.getDescription());
        test.setDuration(createTestDTO.getDuration());
        test.setOpen(createTestDTO.isOpen());
        test.setMaxAttempts(createTestDTO.getMaxAttempts());
        test.setStartTime(createTestDTO.getStartTime());
        test.setEndTime(createTestDTO.getEndTime());

        List<Group> selectedGroups = new ArrayList<>();

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            test.setCreatedByAdmin(true);

            if (createTestDTO.getSubjectId() != null) {
                Subject subject = subjectRepository.findById(createTestDTO.getSubjectId())
                        .orElseThrow(() -> new RuntimeException("Subject not found"));

                if (subject.getGroups() != null && !subject.getGroups().isEmpty()) {
                    selectedGroups.addAll(subject.getGroups());
                }
            } else if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> new RuntimeException("Group not found"));
                    selectedGroups.add(group);
                }
            }
        } else if (currentUser.getRole() == RolesEnum.TEACHER) {
            test.setCreatedByAdmin(false);

            if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> new RuntimeException("Group not found"));

                    if (teacherGroups.contains(group)) {
                        selectedGroups.add(group);
                    }
                }

                if (selectedGroups.isEmpty()) {
                    throw new RuntimeException("No valid groups selected");
                }
            }
        }

        Test savedTest = testRepository.save(test);

        for (Group group : selectedGroups) {
            group.getTests().add(savedTest);
            groupRepository.save(group);
        }

        return testDTOMapper.convertToTestPreviewDTO(savedTest, currentUser);
    }

    @Transactional
    public TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Test existingTest = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        if (existingTest.isCreatedByAdmin() && currentUser.getRole() != RolesEnum.ADMIN) {
            throw new RuntimeException("Cannot modify admin-created tests");
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            boolean isTeacherTest = testAccessValidator.isTeacherTestCreator(currentUser, existingTest, teacherGroups);

            if (!isTeacherTest) {
                throw new RuntimeException("Not authorized to update this test");
            }
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

            if (currentUser.getRole() == RolesEnum.ADMIN) {
                if (updateTestDTO.getSubjectId() != null) {

                    Subject subject = subjectRepository.findById(updateTestDTO.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Subject not found"));
                    newSelectedGroups.addAll(subject.getGroups());

                } else if (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty()) {

                    for (Long groupId : updateTestDTO.getGroupIds()) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found"));
                        newSelectedGroups.add(group);
                    }
                }

            } else if (currentUser.getRole() == RolesEnum.TEACHER) {
                if (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty()) {
                    List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                    for (Long groupId : updateTestDTO.getGroupIds()) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found"));

                        if (teacherGroups.contains(group)) {
                            newSelectedGroups.add(group);
                        }
                    }

                    if (newSelectedGroups.isEmpty()) {
                        throw new RuntimeException("No valid groups selected");
                    }
                }
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
    }

    @Transactional
    public void deleteTest(Long testId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Test existingTest = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

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
                throw new RuntimeException("Cannot delete admin-created tests");
            }

            boolean isTeacherTest = testAccessValidator.isTeacherTestCreator(currentUser, existingTest, teacherGroups);
            if (!isTeacherTest) {
                throw new RuntimeException("Cannot delete this test");
            }

            List<Group> testGroups = testDTOMapper.findGroupsByTest(existingTest);
            for (Group group : testGroups) {
                group.getTests().remove(existingTest);
                groupRepository.save(group);
            }

            testRepository.delete(existingTest);
        } else {
            throw new RuntimeException("Not authorized to delete this test");
        }
    }

    @Transactional(readOnly = true)
    public TestSummaryDTO getTestSummary(Long testId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        testAccessValidator.validateTestAccess(currentUser, test);

        return testDTOMapper.convertToTestPreviewDTO(test, currentUser);
    }

    @Transactional(readOnly = true)
    public List<TestSummaryDTO> getTestsBySubject(Long subjectId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Test> tests = new ArrayList<>();
        List<Group> teacherGroups = currentUser.getRole() == RolesEnum.TEACHER
                ? groupRepository.findByTeacher(currentUser)
                : Collections.emptyList();

        for (Group group : subject.getGroups()) {
            if (currentUser.getRole() == RolesEnum.ADMIN) {
                tests.addAll(group.getTests());
            } else if (currentUser.getRole() == RolesEnum.TEACHER && teacherGroups.contains(group)) {
                tests.addAll(group.getTests());
            }
        }

        return tests.stream()
                .distinct()
                .map(test -> {
                    TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

                    List<Group> testGroups = testDTOMapper.findGroupsByTest(test).stream()
                            .filter(group -> subject.getGroups().contains(group))
                            .toList();

                    List<GroupSummaryDTO> groupDTOs = testGroups.stream()
                            .filter(group -> currentUser.getRole() == RolesEnum.ADMIN ||
                                    (currentUser.getRole() == RolesEnum.TEACHER && teacherGroups.contains(group)))
                            .map(group -> GroupSummaryDTO.builder()
                                    .id(group.getId())
                                    .name(group.getName())
                                    .build())
                            .collect(Collectors.toList());

                    dto.setAssociatedGroups(groupDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestSummaryDTO> getTestsByGroup(Long groupId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        testAccessValidator.validateGroupAccess(currentUser, group);

        return group.getTests().stream()
                .map(test -> {
                    TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

                    List<GroupSummaryDTO> groupDTOs = new ArrayList<>();
                    groupDTOs.add(GroupSummaryDTO.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build());

                    dto.setAssociatedGroups(groupDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleTestActivity(Long testId, Principal principal) {
        log.info("User {} is attempting to toggle activity for test with ID {}", principal.getName(), testId);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return new RuntimeException("User not found");
                });

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.error("Test with ID {} not found", testId);
                    return new RuntimeException("Test not found");
                });

        if (currentUser.getRole() != RolesEnum.ADMIN) {
            log.info("User {} is not an admin, checking permissions...", currentUser.getUsername());

            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            boolean isTeacherTest = testAccessValidator.isTeacherTestCreator(currentUser, test, teacherGroups);

            if (!isTeacherTest) {
                log.warn("User {} is not authorized to change test activity for test {}", currentUser.getUsername(), testId);
                throw new RuntimeException("Not authorized to change test activity");
            }

            if (test.isCreatedByAdmin()) {
                log.warn("User {} attempted to modify an admin-created test {}", currentUser.getUsername(), testId);
                throw new RuntimeException("Cannot modify admin-created tests");
            }
        }

        boolean newState = !test.isOpen();
        test.setOpen(newState);
        testRepository.save(test);

        log.info("Test ID {} activity toggled to {} by user {}", testId, newState, currentUser.getUsername());
    }
}