package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.*;
import com.altester.core.repository.*;
import com.altester.core.service.AttemptRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptRetrievalServiceImpl implements AttemptRetrievalService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AttemptRepository attemptRepository;
    private final TestRepository testRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TestAttemptsForGroupDTO> getTestAttemptsForTeacher(
            Principal principal, Long testId, String searchQuery) {
        log.info("Teacher {} requesting attempts for test {}, search query: {}",
                principal.getName(), testId, searchQuery);

        User teacher = getUserFromPrincipal(principal);

        if (teacher.getRole() != RolesEnum.TEACHER) {
            throw AccessDeniedException.roleConflict();
        }

        testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", testId.toString(), null));

        List<Group> teacherGroups = groupRepository.findByTeacher(teacher).stream()
                .filter(group -> group.getTests().stream()
                        .anyMatch(t -> t.getId() == testId))
                .toList();

        return processGroupsForTestAttempts(teacherGroups, testId, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestAttemptsForGroupDTO> getTestAttemptsForAdmin(
            Principal principal, Long testId, String searchQuery) {
        log.info("Admin {} requesting attempts for test {}, search query: {}",
                principal.getName(), testId, searchQuery);

        User admin = getUserFromPrincipal(principal);

        if (admin.getRole() != RolesEnum.ADMIN) {
            throw AccessDeniedException.roleConflict();
        }

        testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", testId.toString(), null));

        List<Group> groups = groupRepository.findAll().stream()
                .filter(group -> group.getTests().stream()
                        .anyMatch(t -> t.getId() == testId))
                .toList();

        return processGroupsForTestAttempts(groups, testId, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestAttemptsResponseDTO getStudentAttemptsForTeacher(
            Principal principal, String username, String searchQuery) {
        log.info("Teacher {} requesting attempts for student with username {}, search query: {}",
                principal.getName(), username, searchQuery);

        User teacher = getUserFromPrincipal(principal);

        if (teacher.getRole() != RolesEnum.TEACHER) {
            throw AccessDeniedException.roleConflict();
        }

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.user(username));

        List<Group> teacherGroups = groupRepository.findByTeacher(teacher).stream()
                .filter(group -> group.getStudents().contains(student))
                .toList();

        List<Attempt> allAttempts = new ArrayList<>();
        for (Group group : teacherGroups) {
            allAttempts.addAll(
                    attemptRepository.findAll().stream()
                            .filter(attempt -> attempt.getStudent().equals(student) &&
                                    group.getStudents().contains(attempt.getStudent()))
                            .toList()
            );
        }

        return processAttemptsForStudent(allAttempts, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestAttemptsResponseDTO getStudentAttemptsForAdmin(
            Principal principal, String username, String searchQuery) {
        log.info("Admin {} requesting attempts for student with username {}, search query: {}",
                principal.getName(), username, searchQuery);

        User admin = getUserFromPrincipal(principal);

        if (admin.getRole() != RolesEnum.ADMIN) {
            throw AccessDeniedException.roleConflict();
        }

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.user(username));

        List<Attempt> allAttempts = attemptRepository.findAll().stream()
                .filter(attempt -> attempt.getStudent().equals(student))
                .toList();

        return processAttemptsForStudent(allAttempts, searchQuery);
    }

    private StudentTestAttemptsResponseDTO processAttemptsForStudent(List<Attempt> attempts, String searchQuery) {
        if (StringUtils.hasText(searchQuery)) {
            String query = searchQuery.toLowerCase();
            attempts = attempts.stream()
                    .filter(attempt -> {
                        String testTitle = attempt.getTest().getTitle().toLowerCase();
                        return testTitle.contains(query);
                    })
                    .toList();
        }

        if (attempts.isEmpty()) {
            return new StudentTestAttemptsResponseDTO(Collections.emptyList());
        }

        Map<Test, List<Attempt>> attemptsByTest = attempts.stream()
                .collect(Collectors.groupingBy(Attempt::getTest));

        List<StudentTestAttemptDTO> testAttempts = new ArrayList<>();

        for (Map.Entry<Test, List<Attempt>> entry : attemptsByTest.entrySet()) {
            Test test = entry.getKey();
            List<Attempt> testAttemptsForTest = entry.getValue();

            List<AttemptInfoDTO> attemptInfos = testAttemptsForTest.stream()
                    .map(attempt -> AttemptInfoDTO.builder()
                            .attemptId(attempt.getId())
                            .attemptNumber(attempt.getAttemptNumber())
                            .startTime(attempt.getStartTime())
                            .endTime(attempt.getEndTime())
                            .score(attempt.getScore())
                            .status(attempt.getStatus().name())
                            .build())
                    .sorted(Comparator.comparing(AttemptInfoDTO::getAttemptNumber))
                    .collect(Collectors.toList());

            testAttempts.add(StudentTestAttemptDTO.builder()
                    .testId(test.getId())
                    .testName(test.getTitle())
                    .attempts(attemptInfos)
                    .build());
        }

        return new StudentTestAttemptsResponseDTO(testAttempts);
    }

    private List<TestAttemptsForGroupDTO> processGroupsForTestAttempts(
            List<Group> groups, Long testId, String searchQuery) {

        List<TestAttemptsForGroupDTO> groupAttempts = new ArrayList<>();

        for (Group group : groups) {
            List<Attempt> attempts = attemptRepository.findAll().stream()
                    .filter(attempt ->
                            attempt.getTest().getId() == testId &&
                                    group.getStudents().contains(attempt.getStudent()))
                    .collect(Collectors.toList());

            if (StringUtils.hasText(searchQuery)) {
                String query = searchQuery.toLowerCase();
                attempts = attempts.stream()
                        .filter(attempt -> {
                            User student = attempt.getStudent();
                            return student.getUsername().toLowerCase().contains(query) ||
                                    student.getName().toLowerCase().contains(query) ||
                                    student.getSurname().toLowerCase().contains(query);
                        })
                        .toList();
            }

            if (attempts.isEmpty()) {
                continue;
            }

            Map<User, List<Attempt>> attemptsByStudent = attempts.stream()
                    .collect(Collectors.groupingBy(Attempt::getStudent));

            List<StudentAttemptGroup> studentAttemptGroups = attemptsByStudent.entrySet().stream()
                    .map(entry -> {
                        User student = entry.getKey();
                        List<Attempt> studentAttempts = entry.getValue();

                        studentAttempts.sort(Comparator.comparing(Attempt::getAttemptNumber));

                        List<AttemptInfoDTO> attemptInfos = studentAttempts.stream()
                                .map(attempt -> AttemptInfoDTO.builder()
                                        .attemptId(attempt.getId())
                                        .attemptNumber(attempt.getAttemptNumber())
                                        .startTime(attempt.getStartTime())
                                        .endTime(attempt.getEndTime())
                                        .score(attempt.getScore())
                                        .status(attempt.getStatus().name())
                                        .build())
                                .collect(Collectors.toList());

                        return StudentAttemptGroup.builder()
                                .username(student.getUsername())
                                .firstName(student.getName())
                                .lastName(student.getSurname())
                                .attempts(attemptInfos)
                                .build();
                    })
                    .sorted(Comparator.comparing(StudentAttemptGroup::getUsername))
                    .collect(Collectors.toList());

            groupAttempts.add(TestAttemptsForGroupDTO.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .students(studentAttemptGroups)
                    .build());
        }

        return groupAttempts;
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
    }
}