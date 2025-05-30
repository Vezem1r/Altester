package com.altester.core.serviceImpl.attemptRetrieval;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Submission;
import com.altester.core.model.subject.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptDataProcessor {

  /** Process attempts for a student, optionally filtering by search query. */
  public StudentTestAttemptsResponseDTO processAttemptsForStudent(
      List<Attempt> attempts, String searchQuery) {
    if (StringUtils.hasText(searchQuery)) {
      String query = searchQuery.toLowerCase();
      attempts =
          attempts.stream()
              .filter(
                  attempt -> {
                    String testTitle = attempt.getTest().getTitle().toLowerCase();
                    return testTitle.contains(query);
                  })
              .toList();
    }

    if (attempts.isEmpty()) {
      return new StudentTestAttemptsResponseDTO(Collections.emptyList());
    }

    Map<Test, List<Attempt>> attemptsByTest =
        attempts.stream().collect(Collectors.groupingBy(Attempt::getTest));

    List<StudentTestAttemptDTO> testAttempts = new ArrayList<>();

    for (Map.Entry<Test, List<Attempt>> entry : attemptsByTest.entrySet()) {
      Test test = entry.getKey();
      List<Attempt> testAttemptsForTest = entry.getValue();

      List<AttemptInfoDTO> attemptInfos =
          testAttemptsForTest.stream()
              .map(
                  attempt ->
                      AttemptInfoDTO.builder()
                          .attemptId(attempt.getId())
                          .attemptNumber(attempt.getAttemptNumber())
                          .startTime(attempt.getStartTime())
                          .endTime(attempt.getEndTime())
                          .score(attempt.getScore())
                          .aiScore(attempt.getAiScore())
                          .status(attempt.getStatus().name())
                          .build())
              .sorted(Comparator.comparing(AttemptInfoDTO::getAttemptNumber))
              .toList();

      testAttempts.add(
          StudentTestAttemptDTO.builder()
              .testId(test.getId())
              .testName(test.getTitle())
              .attempts(attemptInfos)
              .build());
    }

    return new StudentTestAttemptsResponseDTO(testAttempts);
  }

  /** Process groups for test attempts, optionally filtering by search query. */
  public List<TestAttemptsForGroupDTO> processGroupsForTestAttempts(
      List<Group> groups, Long testId, List<Attempt> allAttempts, String searchQuery) {

    List<TestAttemptsForGroupDTO> groupAttempts = new ArrayList<>();

    for (Group group : groups) {
      List<Attempt> attempts =
          allAttempts.stream()
              .filter(
                  attempt ->
                      attempt.getTest().getId() == testId
                          && group.getStudents().contains(attempt.getStudent()))
              .toList();

      if (StringUtils.hasText(searchQuery)) {
        String query = searchQuery.toLowerCase();
        attempts =
            attempts.stream()
                .filter(
                    attempt -> {
                      User student = attempt.getStudent();
                      return student.getUsername().toLowerCase().contains(query)
                          || student.getName().toLowerCase().contains(query)
                          || student.getSurname().toLowerCase().contains(query);
                    })
                .toList();
      }

      if (attempts.isEmpty()) {
        continue;
      }

      Map<User, List<Attempt>> attemptsByStudent =
          attempts.stream().collect(Collectors.groupingBy(Attempt::getStudent));

      List<StudentAttemptGroup> studentAttemptGroups =
          attemptsByStudent.entrySet().stream()
              .map(
                  entry -> {
                    User student = entry.getKey();
                    List<Attempt> studentAttempts = entry.getValue();

                    double avgScore =
                        studentAttempts.stream()
                            .filter(
                                attempt ->
                                    attempt.getSubmissions().stream()
                                        .map(Submission::getScore)
                                        .anyMatch(Objects::nonNull))
                            .mapToInt(
                                attempt ->
                                    attempt.getSubmissions().stream()
                                        .map(Submission::getScore)
                                        .filter(Objects::nonNull)
                                        .mapToInt(Integer::intValue)
                                        .sum())
                            .average()
                            .orElse(0.0);

                    avgScore =
                        BigDecimal.valueOf(avgScore)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();

                    double avgAiScore =
                        studentAttempts.stream()
                            .filter(
                                attempt ->
                                    attempt.getSubmissions().stream()
                                        .map(Submission::getAiScore)
                                        .anyMatch(Objects::nonNull))
                            .mapToInt(
                                attempt ->
                                    attempt.getSubmissions().stream()
                                        .map(Submission::getAiScore)
                                        .filter(Objects::nonNull)
                                        .mapToInt(Integer::intValue)
                                        .sum())
                            .average()
                            .orElse(0.0);

                    avgAiScore =
                        BigDecimal.valueOf(avgAiScore)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();

                    studentAttempts.sort(Comparator.comparing(Attempt::getAttemptNumber));

                    return StudentAttemptGroup.builder()
                        .username(student.getUsername())
                        .firstName(student.getName())
                        .lastName(student.getSurname())
                        .attemptCount(studentAttempts.size())
                        .averageScore(avgScore)
                        .averageAiScore(avgAiScore)
                        .build();
                  })
              .sorted(Comparator.comparing(StudentAttemptGroup::getUsername))
              .toList();

      groupAttempts.add(
          TestAttemptsForGroupDTO.builder()
              .groupId(group.getId())
              .groupName(group.getName())
              .students(studentAttemptGroups)
              .build());
    }

    return groupAttempts;
  }

  public List<AttemptInfoDTO> processStudentTestAttempts(List<Attempt> attempts) {

    return attempts.stream()
        .map(
            attempt ->
                AttemptInfoDTO.builder()
                    .attemptId(attempt.getId())
                    .attemptNumber(attempt.getAttemptNumber())
                    .startTime(attempt.getStartTime())
                    .endTime(attempt.getEndTime())
                    .score(attempt.getScore())
                    .aiScore(attempt.getAiScore())
                    .status(attempt.getStatus().name())
                    .build())
        .sorted(Comparator.comparing(AttemptInfoDTO::getAttemptNumber))
        .toList();
  }
}
