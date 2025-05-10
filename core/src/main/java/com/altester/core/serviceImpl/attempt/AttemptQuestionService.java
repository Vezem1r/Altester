package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.AnswerDTO;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Submission;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.model.subject.enums.QuestionType;
import com.altester.core.repository.OptionRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptQuestionService {

  private final OptionRepository optionRepository;

  /**
   * Retrieves and randomizes questions for a test based on difficulty settings. If specific counts
   * for each difficulty are set, selects that many questions from each difficulty level. Otherwise,
   * falls back to the maximum questions setting.
   *
   * @param test The test entity containing questions and configuration
   * @return A randomized list of selected questions
   */
  public List<Question> getQuestionsForTest(Test test) {
    Map<QuestionDifficulty, List<Question>> questionsByDifficulty =
        test.getQuestions().stream().collect(Collectors.groupingBy(Question::getDifficulty));

    List<Question> selectedQuestions = new ArrayList<>();

    boolean hasDifficultyDistribution =
        (test.getEasyQuestionsCount() != null && test.getEasyQuestionsCount() > 0)
            || (test.getMediumQuestionsCount() != null && test.getMediumQuestionsCount() > 0)
            || (test.getHardQuestionsCount() != null && test.getHardQuestionsCount() > 0);

    if (hasDifficultyDistribution) {
      log.debug("Using difficulty distribution for test ID: {}", test.getId());

      addQuestionsForDifficulty(
          selectedQuestions,
          questionsByDifficulty,
          QuestionDifficulty.EASY,
          test.getEasyQuestionsCount());

      addQuestionsForDifficulty(
          selectedQuestions,
          questionsByDifficulty,
          QuestionDifficulty.MEDIUM,
          test.getMediumQuestionsCount());

      addQuestionsForDifficulty(
          selectedQuestions,
          questionsByDifficulty,
          QuestionDifficulty.HARD,
          test.getHardQuestionsCount());
    } else {
      log.debug(
          "No difficulty distribution specified for test ID: {}, using all questions",
          test.getId());
      selectedQuestions.addAll(test.getQuestions());
    }

    Collections.shuffle(selectedQuestions);
    log.info(
        "Total of {} questions selected for test ID: {}", selectedQuestions.size(), test.getId());
    return selectedQuestions;
  }

  /**
   * Adds questions of a specific difficulty to the selected questions list.
   *
   * @param selectedQuestions The list to add selected questions to
   * @param questionsByDifficulty Map of questions grouped by difficulty
   * @param difficulty The difficulty level to process
   * @param count The requested number of questions for this difficulty
   */
  private void addQuestionsForDifficulty(
      List<Question> selectedQuestions,
      Map<QuestionDifficulty, List<Question>> questionsByDifficulty,
      QuestionDifficulty difficulty,
      Integer count) {

    if (count == null || count <= 0) {
      return;
    }

    List<Question> questionsOfDifficulty =
        questionsByDifficulty.getOrDefault(difficulty, Collections.emptyList());
    log.debug(
        "Found {} {} questions in test",
        questionsOfDifficulty.size(),
        difficulty.toString().toLowerCase());

    if (!questionsOfDifficulty.isEmpty()) {
      List<Question> selectedQuestionsOfDifficulty =
          getRandomQuestions(questionsOfDifficulty, count);
      log.debug(
          "Selected {} {} questions out of {} available",
          selectedQuestionsOfDifficulty.size(),
          difficulty.toString().toLowerCase(),
          questionsOfDifficulty.size());
      selectedQuestions.addAll(selectedQuestionsOfDifficulty);
    } else {
      log.warn(
          "Test requires {} {} questions but none are available",
          count,
          difficulty.toString().toLowerCase());
    }
  }

  /**
   * Selects a random subset of questions from a list. If count is greater than or equal to the
   * available questions, all questions will be selected.
   *
   * @param questions The list of questions to select from
   * @param count The number of questions to select
   * @return A list of randomly selected questions
   */
  private List<Question> getRandomQuestions(List<Question> questions, int count) {
    if (questions.isEmpty()) {
      return Collections.emptyList();
    }

    if (count >= questions.size()) {
      log.debug(
          "Requested {} questions but only {} are available, returning all",
          count,
          questions.size());
      return new ArrayList<>(questions);
    }

    List<Question> questionsCopy = new ArrayList<>(questions);
    Collections.shuffle(questionsCopy);
    List<Question> selectedQuestions = questionsCopy.subList(0, count);

    log.debug("Randomly selected {} questions from a pool of {}", count, questions.size());
    return selectedQuestions;
  }

  /**
   * Creates empty submissions for all selected questions in an attempt
   *
   * @param attempt The attempt to create submissions for
   * @param questions The list of selected questions for the attempt
   */
  public void createInitialSubmissions(Attempt attempt, List<Question> questions) {
    List<Submission> submissions = new ArrayList<>();

    for (int i = 0; i < questions.size(); i++) {
      Question question = questions.get(i);
      Submission submission =
          Submission.builder()
              .attempt(attempt)
              .question(question)
              .orderIndex(i)
              .selectedOptions(new ArrayList<>())
              .aiGraded(false)
              .build();

      submissions.add(submission);
    }

    attempt.setSubmissions(submissions);
    log.debug(
        "Created {} initial empty submissions for attempt ID: {}",
        submissions.size(),
        attempt.getId());
  }

  /**
   * Gets a list of questions from the submissions of an attempt
   *
   * @param attempt The attempt containing submissions
   * @return A list of questions in the order they appear in the submissions
   */
  public List<Question> getQuestionsFromSubmissions(Attempt attempt) {
    if (attempt.getSubmissions() == null || attempt.getSubmissions().isEmpty()) {
      return Collections.emptyList();
    }

    return attempt.getSubmissions().stream()
        .sorted(Comparator.comparing(Submission::getOrderIndex))
        .map(Submission::getQuestion)
        .filter(Objects::nonNull)
        .toList();
  }

  /** Identifies the next unanswered question in an ongoing test attempt. */
  public int findQuestionToResume(Attempt attempt, List<Question> questions) {
    if (attempt.getSubmissions() == null || attempt.getSubmissions().isEmpty()) {
      return 1;
    }

    Set<Long> answeredQuestionIds =
        attempt.getSubmissions().stream()
            .filter(
                s ->
                    (s.getSelectedOptions() != null && !s.getSelectedOptions().isEmpty())
                        || (s.getAnswerText() != null && !s.getAnswerText().isEmpty()))
            .map(s -> s.getQuestion().getId())
            .collect(Collectors.toSet());

    for (int i = 0; i < questions.size(); i++) {
      if (!answeredQuestionIds.contains(questions.get(i).getId())) {
        return i + 1;
      }
    }
    return questions.size();
  }

  public int findQuestionIndex(List<Question> questions, Question question) {
    return questions.stream().map(Question::getId).toList().indexOf(question.getId());
  }

  public void updateSubmission(Submission submission, AnswerDTO answerDTO, Question question) {
    submission.setSelectedOptions(new ArrayList<>());
    submission.setAnswerText(null);

    QuestionType questionType = question.getQuestionType();

    if (questionType == QuestionType.MULTIPLE_CHOICE
        || questionType == QuestionType.IMAGE_WITH_MULTIPLE_CHOICE) {
      processMultipleChoiceAnswer(submission, answerDTO);
    } else {
      submission.setAnswerText(answerDTO.getAnswerText());
    }
  }

  private void processMultipleChoiceAnswer(Submission submission, AnswerDTO answerDTO) {
    if (answerDTO.getSelectedOptionIds() == null || answerDTO.getSelectedOptionIds().isEmpty()) {
      return;
    }

    List<Option> selectedOptions =
        answerDTO.getSelectedOptionIds().stream().map(this::findOptionById).toList();

    submission.getSelectedOptions().addAll(selectedOptions);
  }

  private Option findOptionById(Long optionId) {
    return optionRepository
        .findById(optionId)
        .orElseThrow(
            () ->
                new com.altester.core.exception.ResourceNotFoundException(
                    "Option", optionId.toString(), null));
  }
}
