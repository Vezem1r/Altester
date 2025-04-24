package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.dtos.core_service.review.AttemptReviewSubmissionDTO;
import com.altester.core.dtos.core_service.review.QuestionReviewSubmissionDTO;
import com.altester.core.dtos.core_service.student.AttemptReviewDTO;
import com.altester.core.dtos.core_service.student.OptionReviewDTO;
import com.altester.core.dtos.core_service.student.QuestionReviewDTO;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.repository.*;
import com.altester.core.service.AttemptRetrievalService;
import com.altester.core.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final NotificationDispatchService notificationService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "testAttemptsForTeacher",
            key = "#principal.name + ':testId:' + #testId + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
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
    @Cacheable(value = "testAttemptsForAdmin",
            key = "#principal.name + ':testId:' + #testId + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
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
    @Cacheable(value = "studentAttemptsForTeacher",
            key = "#principal.name + ':username:' + #username + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
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
    @Cacheable(value = "studentAttemptsForAdmin",
            key = "#principal.name + ':username:' + #username + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attemptReview", key = "'admin-teacher:' + #principal.name + ':attemptId:' + #attemptId")
    public AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId) {
        log.info("{} requesting review for attempt {}", principal.getName(), attemptId);

        User user = getUserFromPrincipal(principal);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

        verifyAttemptAccessPermission(user, attempt);

        Test test = attempt.getTest();

        List<QuestionReviewDTO> questionReviews = new ArrayList<>();
        for (Submission submission : attempt.getSubmissions()) {
            Question question = submission.getQuestion();

            List<OptionReviewDTO> optionReviews = new ArrayList<>();
            for (Option option : question.getOptions()) {
                boolean isSelected = submission.getSelectedOptions().contains(option);

                optionReviews.add(OptionReviewDTO.builder()
                        .optionId(option.getId())
                        .text(option.getText())
                        .description(option.getDescription())
                        .isSelected(isSelected)
                        .isCorrect(option.isCorrect())
                        .build());
            }

            List<Long> selectedOptionIds = submission.getSelectedOptions().stream()
                    .map(Option::getId)
                    .collect(Collectors.toList());

            questionReviews.add(QuestionReviewDTO.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .imagePath(question.getImagePath())
                    .options(optionReviews)
                    .studentAnswer(submission.getAnswerText())
                    .selectedOptionIds(selectedOptionIds)
                    .score(submission.getScore() != null ? submission.getScore() : 0)
                    .maxScore(question.getScore())
                    .teacherFeedback(submission.getTeacherFeedback())
                    .build());
        }

        return AttemptReviewDTO.builder()
                .attemptId(attempt.getId())
                .testTitle(test.getTitle())
                .testDescription(test.getDescription())
                .score(attempt.getScore() != null ? attempt.getScore() : 0)
                .totalScore(test.getTotalScore())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .questions(questionReviews)
                .build();
    }

    private void verifyAttemptAccessPermission(User user, Attempt attempt) {
        if (user.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (user.getRole() == RolesEnum.TEACHER) {
            User student = attempt.getStudent();
            boolean hasAccess = groupRepository.findByTeacher(user).stream()
                    .anyMatch(group -> group.getStudents().contains(student));

            if (!hasAccess) {
                throw AccessDeniedException.attemptAccess();
            }
        } else {
            throw AccessDeniedException.roleConflict();
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "testAttemptsForTeacher", allEntries = true),
            @CacheEvict(value = "testAttemptsForAdmin", allEntries = true),
            @CacheEvict(value = "studentAttemptsForTeacher", allEntries = true),
            @CacheEvict(value = "studentAttemptsForAdmin", allEntries = true),
            @CacheEvict(value = "attemptReview", key = "'admin-teacher:' + #principal.name + ':attemptId:' + #attemptId"),
            @CacheEvict(value = "attemptReview", key = "#principal.name + ':attemptId:' + #attemptId")
    })
    public void submitAttemptReview(Principal principal, Long attemptId, AttemptReviewSubmissionDTO reviewSubmission) {
        log.info("{} submitting review for attempt {}", principal.getName(), attemptId);

        User user = getUserFromPrincipal(principal);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

        verifyAttemptAccessPermission(user, attempt);

        Map<Long, Submission> submissionMap = attempt.getSubmissions().stream()
                .collect(Collectors.toMap(
                        submission -> submission.getQuestion().getId(),
                        submission -> submission
                ));

        int totalScore = 0;

        for (QuestionReviewSubmissionDTO questionReview : reviewSubmission.getQuestionReviews()) {
            Submission submission = submissionMap.get(questionReview.getQuestionId());

            if (submission != null) {
                submission.setScore(questionReview.getScore());
                submission.setTeacherFeedback(questionReview.getTeacherFeedback());

                totalScore += questionReview.getScore() != null ? questionReview.getScore() : 0;
            }
        }

        attempt.setScore(totalScore);
        attempt.setStatus(AttemptStatus.REVIEWED);
        attemptRepository.save(attempt);

        notificationService.notifyTestGraded(attempt);

        boolean hasFeedback = reviewSubmission.getQuestionReviews().stream()
                .anyMatch(qr -> StringUtils.hasText(qr.getTeacherFeedback()));

        if (hasFeedback) {
            notificationService.notifyTeacherFeedback(attempt);
        }
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