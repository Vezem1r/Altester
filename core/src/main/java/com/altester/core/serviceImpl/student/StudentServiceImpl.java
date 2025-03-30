package com.altester.core.serviceImpl.student;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
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
    private final SubjectRepository subjectRepository;

    @Override
    public StudentDashboardResponse getStudentDashboard(Principal principal, String searchQuery, Long groupId) {
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
                .currentGroups(mapGroupsToDTO(currentGroups, student, searchQuery))
                .build();
    }

    @Override
    public AcademicHistoryResponse getAcademicHistory(
            Principal principal,
            Integer academicYear,
            Semester semester,
            String searchQuery) {

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

    private Map<String, List<Group>> filterGroupsByPeriod(
            Map<String, List<Group>> groupedByPeriod,
            Integer academicYear,
            Semester semester) {

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

    private List<GroupDTO> mapGroupsToDTO(List<Group> groups, User student, String searchQuery) {
        return groups.stream()
                .map(group -> {
                    List<Test> groupTests = new ArrayList<>(group.getTests());

                    Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());

                    String groupName;

                    groupName = subject.map(value -> value.getShortName() + " | " + group.getTeacher().getName() + " " + group.getTeacher().getSurname() + " | " + group.getName())
                            .orElseGet(() -> group.getTeacher().getName() + " " + group.getTeacher().getSurname() + " | " + group.getName());

                    List<Test> openTests = groupTests.stream()
                            .filter(Test::isOpen)
                            .filter(test -> filterTestBySearchQuery(test, searchQuery))
                            .toList();

                    List<TestDTO> testDTOs = openTests.stream()
                            .map(test -> mapTestToDTO(test, student))
                            .collect(Collectors.toList());

                    return GroupDTO.builder()
                            .id(group.getId())
                            .name(groupName)
                            .semester(group.getSemester())
                            .academicYear(group.getAcademicYear())
                            .tests(testDTOs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AcademicHistoryDTO> buildAcademicHistory(
            Map<String, List<Group>> pastGroupsByPeriod,
            User student,
            String searchQuery) {

        List<AcademicHistoryDTO> history = new ArrayList<>();

        for (Map.Entry<String, List<Group>> entry : pastGroupsByPeriod.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Integer academicYear = Integer.parseInt(parts[0]);
            Semester semester = Semester.valueOf(parts[1]);

            List<GroupDTO> groupDTOs = mapGroupsToDTO(entry.getValue(), student, searchQuery);

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

    @Override
    public AvailablePeriodsResponse getAvailablePeriods(Principal principal) {
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

    private TestDTO mapTestToDTO(Test test, User student) {
        List<Attempt> attempts = attemptRepository.findByTestAndStudent(test, student);
        int totalAttempts = attempts.size();
        int maxAttempts = test.getMaxAttempts() != null ? test.getMaxAttempts() : Integer.MAX_VALUE;
        int remainingAttempts = maxAttempts - totalAttempts;

        Integer bestScore = attempts.stream()
                .filter(Attempt::isCompleted)
                .map(Attempt::getScore)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);

        return TestDTO.builder()
                .id(test.getId())
                .title(test.getTitle())
                .duration(test.getDuration())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .maxAttempts(test.getMaxAttempts())
                .remainingAttempts(Math.max(remainingAttempts, 0))
                .totalScore(test.getTotalScore())
                .bestScore(bestScore)
                .build();
    }

    private boolean filterTestBySearchQuery(Test test, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase();
        return test.getTitle().toLowerCase().contains(query) ||
                (test.getDescription() != null && test.getDescription().toLowerCase().contains(query));
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
}