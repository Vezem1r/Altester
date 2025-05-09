package com.altester.core.serviceImpl.test;

import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestRequirementsValidator {

  /**
   * Verifies if a test meets all the difficulty requirements. A test meets the requirements if it
   * has at least the specified number of questions for each difficulty level.
   *
   * @param test The test to validate
   * @return true if all requirements are met, false otherwise
   */
  public boolean requirementsMet(Test test) {
    Map<QuestionDifficulty, Long> questionCounts =
        test.getQuestions().stream()
            .collect(Collectors.groupingBy(Question::getDifficulty, Collectors.counting()));

    log.debug("Test ID {} has the following question counts: {}", test.getId(), questionCounts);

    if (test.getEasyQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.EASY, 0L)
            < test.getEasyQuestionsCount())) {
      log.debug(
          "Test ID {} does not have enough EASY questions. Required: {}, Available: {}",
          test.getId(),
          test.getEasyQuestionsCount(),
          questionCounts.getOrDefault(QuestionDifficulty.EASY, 0L));
      return false;
    }

    if (test.getMediumQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.MEDIUM, 0L)
            < test.getMediumQuestionsCount())) {
      log.debug(
          "Test ID {} does not have enough MEDIUM questions. Required: {}, Available: {}",
          test.getId(),
          test.getMediumQuestionsCount(),
          questionCounts.getOrDefault(QuestionDifficulty.MEDIUM, 0L));
      return false;
    }

    if (test.getHardQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.HARD, 0L)
            < test.getHardQuestionsCount())) {
      log.debug(
          "Test ID {} does not have enough HARD questions. Required: {}, Available: {}",
          test.getId(),
          test.getHardQuestionsCount(),
          questionCounts.getOrDefault(QuestionDifficulty.HARD, 0L));
      return false;
    }

    log.debug("Test ID {} meets all difficulty requirements", test.getId());
    return true;
  }

  /**
   * Gets a description of the missing requirements for error messages
   *
   * @param test The test to check
   * @return A string describing what requirements are not met
   */
  public String getMissingRequirements(Test test) {
    Map<QuestionDifficulty, Long> questionCounts =
        test.getQuestions().stream()
            .collect(Collectors.groupingBy(Question::getDifficulty, Collectors.counting()));

    StringBuilder message = new StringBuilder("Test does not meet the following requirements: ");
    boolean hasIssues = false;

    if (test.getEasyQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.EASY, 0L)
            < test.getEasyQuestionsCount())) {
      message.append(
          String.format(
              "Needs %d EASY questions (has %d)",
              test.getEasyQuestionsCount(),
              questionCounts.getOrDefault(QuestionDifficulty.EASY, 0L)));
      hasIssues = true;
    }

    if (test.getMediumQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.MEDIUM, 0L)
            < test.getMediumQuestionsCount())) {
      if (hasIssues) message.append(", ");
      message.append(
          String.format(
              "Needs %d MEDIUM questions (has %d)",
              test.getMediumQuestionsCount(),
              questionCounts.getOrDefault(QuestionDifficulty.MEDIUM, 0L)));
      hasIssues = true;
    }

    if (test.getHardQuestionsCount() > 0
        && (questionCounts.getOrDefault(QuestionDifficulty.HARD, 0L)
            < test.getHardQuestionsCount())) {
      if (hasIssues) message.append(", ");
      message.append(
          String.format(
              "Needs %d HARD questions (has %d)",
              test.getHardQuestionsCount(),
              questionCounts.getOrDefault(QuestionDifficulty.HARD, 0L)));
    }

    return message.toString();
  }
}
