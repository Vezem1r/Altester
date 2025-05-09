package com.altester.core.serviceImpl.studentPage;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.SubjectRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMapper {

  private final SubjectRepository subjectRepository;
  private final AttemptRepository attemptRepository;

  public List<GroupDTO> mapGroupsToDTO(List<Group> groups, User student, String searchQuery) {
    return groups.stream()
        .map(
            group -> {
              List<Test> groupTests = new ArrayList<>(group.getTests());

              Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());

              String groupName =
                  subject
                      .map(
                          value ->
                              value.getShortName()
                                  + " | "
                                  + group.getTeacher().getName()
                                  + " "
                                  + group.getTeacher().getSurname()
                                  + " | "
                                  + group.getName())
                      .orElseGet(
                          () ->
                              group.getTeacher().getName()
                                  + " "
                                  + group.getTeacher().getSurname()
                                  + " | "
                                  + group.getName());

              List<Test> openTests =
                  groupTests.stream()
                      .filter(Test::isOpen)
                      .filter(test -> filterTestBySearchQuery(test, searchQuery))
                      .filter(this::filterTestByQuestions)
                      .toList();

              List<TestDTO> testDTOs =
                  openTests.stream()
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

  public TestDTO mapTestToDTO(Test test, User student) {
    List<Attempt> attempts = attemptRepository.findByTestAndStudent(test, student);

    int completedAttempts =
        (int)
            attempts.stream()
                .filter(
                    attempt ->
                        attempt.getStatus() == AttemptStatus.COMPLETED
                            || attempt.getStatus() == AttemptStatus.REVIEWED)
                .count();

    boolean hasActiveAttempt =
        attempts.stream().anyMatch(attempt -> attempt.getStatus() == AttemptStatus.IN_PROGRESS);

    int maxAttempts = test.getMaxAttempts() != null ? test.getMaxAttempts() : Integer.MAX_VALUE;
    int remainingAttempts = maxAttempts - completedAttempts;

    if (remainingAttempts <= 0 && hasActiveAttempt) {
      remainingAttempts = 1;
    }

    Integer bestScore =
        attempts.stream()
            .filter(attempt -> attempt.getStatus() == AttemptStatus.REVIEWED)
            .map(Attempt::getScore)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(null);

    AttemptStatus status =
        attempts.stream()
            .map(Attempt::getStatus)
            .filter(attemptStatus -> attemptStatus == AttemptStatus.IN_PROGRESS)
            .findFirst()
            .orElseGet(
                () ->
                    attempts.stream()
                        .filter(attempt -> attempt.getStatus() == AttemptStatus.REVIEWED)
                        .max(
                            Comparator.comparingInt(
                                attempt -> attempt.getScore() != null ? attempt.getScore() : 0))
                        .map(Attempt::getStatus)
                        .orElseGet(
                            () ->
                                attempts.stream()
                                    .map(Attempt::getStatus)
                                    .filter(
                                        attemptStatus -> attemptStatus == AttemptStatus.COMPLETED)
                                    .findFirst()
                                    .orElse(null)));

    int easyCount = (test.getEasyQuestionsCount() != null) ? test.getEasyQuestionsCount() : 0;
    int mediumCount = (test.getMediumQuestionsCount() != null) ? test.getMediumQuestionsCount() : 0;
    int hardCount = (test.getHardQuestionsCount() != null) ? test.getHardQuestionsCount() : 0;

    int numberOfQuestions = easyCount + mediumCount + hardCount;

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
        .numberOfQuestions(numberOfQuestions)
        .status(status)
        .build();
  }

  public QuestionReviewDTO mapSubmissionToQuestionReviewDTO(Submission submission) {
    Question question = submission.getQuestion();
    List<Long> selectedOptionIds = new ArrayList<>();

    if (submission.getSelectedOptions() != null && !submission.getSelectedOptions().isEmpty()) {
      selectedOptionIds =
          submission.getSelectedOptions().stream().map(Option::getId).collect(Collectors.toList());
    }

    List<Long> finalSelectedOptionIds = selectedOptionIds;
    List<OptionReviewDTO> optionReviews =
        question.getOptions().stream()
            .map(
                option -> {
                  boolean isSelected = finalSelectedOptionIds.contains(option.getId());
                  return OptionReviewDTO.builder()
                      .optionId(option.getId())
                      .text(option.getText())
                      .description(option.getDescription())
                      .isSelected(isSelected)
                      .isCorrect(option.isCorrect())
                      .build();
                })
            .collect(Collectors.toList());

    return QuestionReviewDTO.builder()
        .submissionId(submission.getId())
        .questionText(question.getQuestionText())
        .imagePath(question.getImagePath())
        .options(optionReviews)
        .studentAnswer(submission.getAnswerText())
        .selectedOptionIds(selectedOptionIds)
        .score(submission.getScore() != null ? submission.getScore() : 0)
        .maxScore(question.getScore())
        .teacherFeedback(submission.getTeacherFeedback())
        .isAiGraded(submission.isAiGraded())
        .isRequested(submission.isRegradeRequested())
        .build();
  }

  public TestAttemptDTO mapAttemptToBasicDTO(Attempt attempt, Test test) {
    int answeredQuestions = attempt.getSubmissions() != null ? attempt.getSubmissions().size() : 0;

    return TestAttemptDTO.builder()
        .attemptId(attempt.getId())
        .attemptNumber(attempt.getAttemptNumber())
        .startTime(attempt.getStartTime())
        .endTime(attempt.getEndTime())
        .status(attempt.getStatus())
        .score(attempt.getScore())
        .answeredQuestions(answeredQuestions)
        .totalQuestions(test.getTotalQuestions())
        .build();
  }

  private boolean filterTestBySearchQuery(Test test, String searchQuery) {
    if (searchQuery == null || searchQuery.trim().isEmpty()) {
      return true;
    }

    String query = searchQuery.toLowerCase();
    return test.getTitle().toLowerCase().contains(query);
  }

  private boolean filterTestByQuestions(Test test) {
    return !test.getQuestions().isEmpty();
  }
}
