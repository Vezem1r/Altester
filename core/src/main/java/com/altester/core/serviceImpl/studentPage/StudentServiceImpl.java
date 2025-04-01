package com.altester.core.serviceImpl.studentPage;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.*;
import com.altester.core.service.StudentService;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AttemptRepository attemptRepository;
    private final GroupActivityService groupActivityService;
    private final TestRepository testRepository;
    private final StudentMapper studentMapper;

    @Override
    public StudentDashboardResponse getStudentDashboard(Principal principal, String searchQuery, Long groupId) {
        log.info("Getting dashboard for student with searchQuery: {}, groupId: {}", searchQuery, groupId);

        User student = getUserFromPrincipal(principal);
        ensureStudentRole(student);

        List<Group> allStudentGroups = groupRepository.findAllByStudentId(student.getId());

        if (groupId != null) {
            Group selectedGroup = groupRepository.findById(groupId)
                    .orElseThrow(() -> ResourceNotFoundException.group(groupId));

            if (allStudentGroups.stream().noneMatch(g -> g.getId() == selectedGroup.getId())) {
                throw AccessDeniedException.groupAccess();
            }

            allStudentGroups = Collections.singletonList(selectedGroup);
        }

        List<Group> currentGroups = filterCurrentGroups(allStudentGroups);

        return StudentDashboardResponse.builder()
                .username(student.getUsername())
                .name(student.getName())
                .surname(student.getSurname())
                .email(student.getEmail())
                .currentGroups(studentMapper.mapGroupsToDTO(currentGroups, student, searchQuery))
                .build();
    }

    @Override
    public AcademicHistoryResponse getAcademicHistory(Principal principal, Integer academicYear,
            Semester semester, String searchQuery) {
        log.info("Getting academic history for student with academicYear: {}, semester: {}, searchQuery: {}",
                academicYear, semester, searchQuery);

        User student = getUserFromPrincipal(principal);
        ensureStudentRole(student);

        List<Group> allStudentGroups = groupRepository.findAllByStudentId(student.getId());

        List<Group> pastGroups = filterPastGroups(allStudentGroups);

        Map<String, List<Group>> groupedByPeriod = groupByPeriod(pastGroups);

        if (academicYear != null || semester != null) {
            groupedByPeriod = filterGroupsByPeriod(groupedByPeriod, academicYear, semester);
        }

        return AcademicHistoryResponse.builder()
                .username(student.getUsername())
                .name(student.getName())
                .surname(student.getSurname())
                .academicHistory(buildAcademicHistory(groupedByPeriod, student, searchQuery))
                .build();
    }

    @Override
    public StudentAttemptsResponse getStudentTestAttempts(Principal principal, Long testId) {
        log.info("Getting attempts for student for test: {}", testId);

        User student = getUserFromPrincipal(principal);
        ensureStudentRole(student);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> ResourceNotFoundException.test(testId));

        if (!test.isOpen()) {
            throw new StateConflictException("test", "closed", "Cannot access attempts for a closed test");
        }

        validateStudentTestAccess(student, test);

        List<Attempt> attempts = attemptRepository.findByTestAndStudent(test, student);

        List<TestAttemptDTO> attemptDTOs = attempts.stream()
                .filter(attempt -> attempt.getStatus() != AttemptStatus.IN_PROGRESS)
                .sorted(Comparator.comparingInt(Attempt::getAttemptNumber))
                .map(attempt -> studentMapper.mapAttemptToBasicDTO(attempt, test))
                .collect(Collectors.toList());

        return StudentAttemptsResponse.builder()
                .testId(test.getId())
                .testTitle(test.getTitle())
                .totalScore(test.getTotalScore())
                .attempts(attemptDTOs)
                .build();
    }

    @Override
    public AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId) {
        log.info("Getting detailed review for attempt: {}", attemptId);

        User student = getUserFromPrincipal(principal);
        ensureStudentRole(student);

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

        validateAttemptOwnership(attempt, student);

        Test test = attempt.getTest();

        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new StateConflictException("attempt", "in_progress", "Cannot view review for an in-progress attempt");
        }

        List<QuestionReviewDTO> questionReviews;

        if (attempt.getStatus() == AttemptStatus.REVIEWED) {
            questionReviews = attempt.getSubmissions().stream()
                    .map(studentMapper::mapSubmissionToQuestionReviewDTO)
                    .collect(Collectors.toList());
        } else {
            questionReviews = attempt.getSubmissions().stream()
                    .map(submission -> {
                        Question question = submission.getQuestion();
                        return QuestionReviewDTO.builder()
                                .questionId(question.getId())
                                .questionText(question.getQuestionText())
                                .imagePath(question.getImagePath())
                                .score(submission.getScore() != null ? submission.getScore() : 0)
                                .maxScore(question.getScore())
                                .build();
                    })
                    .collect(Collectors.toList());
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

    @Override
    public AvailablePeriodsResponse getAvailablePeriods(Principal principal) {
        log.info("Getting available academic periods for student");

        User student = getUserFromPrincipal(principal);
        ensureStudentRole(student);

        List<Group> allStudentGroups = groupRepository.findAllByStudentId(student.getId());

        List<Group> pastGroups = filterPastGroups(allStudentGroups);

        Set<AcademicPeriod> uniquePeriods = new HashSet<>();

        for (Group group : pastGroups) {
            uniquePeriods.add(AcademicPeriod.builder()
                    .academicYear(group.getAcademicYear())
                    .semester(group.getSemester())
                    .build());
        }

        List<AcademicPeriod> sortedPeriods = uniquePeriods.stream()
                .sorted((p1, p2) -> {
                    int yearCompare = p2.getAcademicYear().compareTo(p1.getAcademicYear());
                    if (yearCompare != 0) {
                        return yearCompare;
                    }
                    return p2.getSemester().compareTo(p1.getSemester());
                })
                .collect(Collectors.toList());

        return AvailablePeriodsResponse.builder()
                .username(student.getUsername())
                .periods(sortedPeriods)
                .build();
    }

    private List<Group> filterCurrentGroups(List<Group> groups) {
        List<Group> currentGroups = new ArrayList<>();

        for (Group group : groups) {
            groupActivityService.checkAndUpdateGroupActivity(group);

            if (group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                currentGroups.add(group);
            }
        }

        return currentGroups;
    }

    private List<Group> filterPastGroups(List<Group> groups) {
        List<Group> pastGroups = new ArrayList<>();

        for (Group group : groups) {
            groupActivityService.checkAndUpdateGroupActivity(group);

            if (!group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                pastGroups.add(group);
            }
        }

        return pastGroups;
    }

    private Map<String, List<Group>> groupByPeriod(List<Group> groups) {
        Map<String, List<Group>> groupedByPeriod = new HashMap<>();

        for (Group group : groups) {
            String key = group.getAcademicYear() + "-" + group.getSemester().toString();
            groupedByPeriod.computeIfAbsent(key, k -> new ArrayList<>()).add(group);
        }

        return groupedByPeriod;
    }

    private Map<String, List<Group>> filterGroupsByPeriod(Map<String, List<Group>> groupedByPeriod,
            Integer academicYear, Semester semester) {

        if (academicYear == null && semester == null) {
            return groupedByPeriod;
        }

        Map<String, List<Group>> filtered = new HashMap<>();

        for (Map.Entry<String, List<Group>> entry : groupedByPeriod.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Integer year = Integer.parseInt(parts[0]);
            Semester sem = Semester.valueOf(parts[1]);

            boolean yearMatches = academicYear == null || year.equals(academicYear);
            boolean semesterMatches = semester == null || sem.equals(semester);

            if (yearMatches && semesterMatches) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        return filtered;
    }

    private List<AcademicHistoryDTO> buildAcademicHistory(Map<String, List<Group>> pastGroupsByPeriod,
            User student, String searchQuery) {

        List<AcademicHistoryDTO> history = new ArrayList<>();

        for (Map.Entry<String, List<Group>> entry : pastGroupsByPeriod.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Integer academicYear = Integer.parseInt(parts[0]);
            Semester semester = Semester.valueOf(parts[1]);

            List<GroupDTO> groupDTOs = studentMapper.mapGroupsToDTO(entry.getValue(), student, searchQuery);

            history.add(AcademicHistoryDTO.builder()
                    .semester(semester)
                    .academicYear(academicYear)
                    .groups(groupDTOs)
                    .build());
        }

        history.sort((h1, h2) -> {
            int yearCompare = h2.getAcademicYear().compareTo(h1.getAcademicYear());
            if (yearCompare != 0) {
                return yearCompare;
            }
            return h2.getSemester().compareTo(h1.getSemester());
        });

        return history;
    }

    private void validateAttemptOwnership(Attempt attempt, User student) {
        if (!Objects.equals(attempt.getStudent().getId(), student.getId())) {
            throw AccessDeniedException.testAccess();
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
    }

    private void ensureStudentRole(User user) {
        if (user.getRole() != RolesEnum.STUDENT) {
            throw AccessDeniedException.roleConflict();
        }
    }

    private void validateStudentTestAccess(User student, Test test) {
        List<Group> studentGroups = groupRepository.findAllByStudentId(student.getId());

        boolean isTestInStudentGroup = studentGroups.stream()
                .anyMatch(group -> group.getTests().stream()
                        .anyMatch(t -> t.getId() == test.getId()));

        if (!isTestInStudentGroup) {
            throw AccessDeniedException.testAccess();
        }
    }
}