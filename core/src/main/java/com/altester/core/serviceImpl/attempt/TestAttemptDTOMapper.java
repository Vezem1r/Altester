package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestAttemptDTOMapper {

  private final AttemptQuestionService questionService;
  private final Random random = new Random();

  public QuestionDTO mapQuestionToDTO(Question question) {
    List<OptionDTO> optionDTOs =
        question.getOptions().stream()
            .map(
                option ->
                    OptionDTO.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .description(option.getDescription())
                        .build())
            .toList();

    return QuestionDTO.builder()
        .id(question.getId())
        .questionText(question.getQuestionText())
        .imagePath(question.getImagePath())
        .score(question.getScore())
        .questionType(question.getQuestionType())
        .options(optionDTOs)
        .build();
  }

  public AnswerDTO mapSubmissionToAnswerDTO(Submission submission) {
    Question question = submission.getQuestion();
    QuestionType questionType = question.getQuestionType();

    return switch (questionType) {
      case MULTIPLE_CHOICE, IMAGE_WITH_MULTIPLE_CHOICE -> {
        List<Long> selectedOptionIds =
            submission.getSelectedOptions() != null
                ? submission.getSelectedOptions().stream().map(Option::getId).toList()
                : new ArrayList<>();

        yield AnswerDTO.builder()
            .questionId(question.getId())
            .selectedOptionIds(selectedOptionIds)
            .build();
      }
      case TEXT_ONLY, TEXT_WITH_IMAGE, IMAGE_ONLY ->
          AnswerDTO.builder()
              .questionId(question.getId())
              .answerText(submission.getAnswerText())
              .build();
    };
  }

  public SingleQuestionResponse buildSingleQuestionResponse(
      Attempt attempt,
      int questionNumber,
      List<Question> questions,
      Question question,
      AnswerDTO currentAnswer) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expirationTime =
        attempt.getStartTime().plusMinutes(attempt.getTest().getDuration());
    int timeRemainingSeconds = (int) Duration.between(now, expirationTime).getSeconds();
    if (timeRemainingSeconds < 0) timeRemainingSeconds = 0;

    return SingleQuestionResponse.builder()
        .attemptId(attempt.getId())
        .testTitle(attempt.getTest().getTitle())
        .testDescription(attempt.getTest().getDescription())
        .duration(attempt.getTest().getDuration())
        .startTime(attempt.getStartTime())
        .endTime(expirationTime)
        .totalQuestions(questions.size())
        .currentQuestionNumber(questionNumber)
        .question(mapQuestionToDTO(question))
        .currentAnswer(currentAnswer)
        .isCompleted(attempt.getStatus() == AttemptStatus.COMPLETED)
        .isExpired(now.isAfter(expirationTime))
        .timeRemainingSeconds(timeRemainingSeconds)
        .build();
  }

  public SingleQuestionResponse getQuestionByNumber(
      Attempt attempt, int questionNumber, List<Question> questions) {
    Question question = questions.get(questionNumber - 1);

    AnswerDTO currentAnswer = generateRandomAnswer(question);

    return buildSingleQuestionResponse(attempt, questionNumber, questions, question, currentAnswer);
  }

  private AnswerDTO generateRandomAnswer(Question question) {
    if (random.nextBoolean()) {
      return null;
    }

    QuestionType questionType = question.getQuestionType();

    return switch (questionType) {
      case MULTIPLE_CHOICE, IMAGE_WITH_MULTIPLE_CHOICE -> {
        if (!question.getOptions().isEmpty()) {
          List<Long> selectedIds = new ArrayList<>();
          Option randomOption =
              question.getOptions().get(random.nextInt(question.getOptions().size()));
          selectedIds.add(randomOption.getId());

          yield AnswerDTO.builder()
              .questionId(question.getId())
              .selectedOptionIds(selectedIds)
              .build();
        }
        yield null;
      }
      case TEXT_ONLY, TEXT_WITH_IMAGE, IMAGE_ONLY ->
          AnswerDTO.builder()
              .questionId(question.getId())
              .answerText("Demo answer for question " + question.getId())
              .build();
    };
  }

  public AttemptResultResponse buildAttemptResult(Attempt attempt) {
    Test test = attempt.getTest();
    List<Question> questionsForAttempt = questionService.getQuestionsFromSubmissions(attempt);

    int answeredQuestions = 0;
    if (attempt.getSubmissions() != null) {
      answeredQuestions =
          (int)
              attempt.getSubmissions().stream()
                  .filter(
                      s ->
                          (s.getSelectedOptions() != null && !s.getSelectedOptions().isEmpty())
                              || (s.getAnswerText() != null && !s.getAnswerText().isEmpty()))
                  .count();
    }

    return AttemptResultResponse.builder()
        .attemptId(attempt.getId())
        .testTitle(test.getTitle())
        .score(attempt.getAiScore() != null ? attempt.getAiScore() : 0)
        .totalScore(test.getTotalScore())
        .questionsAnswered(answeredQuestions)
        .totalQuestions(questionsForAttempt.size())
        .status(attempt.getStatus())
        .build();
  }

  public AttemptResultResponse buildDemoAttemptResult(Attempt attempt, List<Question> questions) {
    Test test = attempt.getTest();
    int randomAnsweredQuestions = random.nextInt(questions.size()) + 1;

    return AttemptResultResponse.builder()
        .attemptId(attempt.getId())
        .testTitle(test.getTitle() + " (DEMO)")
        .score(attempt.getAiScore() != null ? attempt.getAiScore() : 0)
        .totalScore(test.getTotalScore())
        .questionsAnswered(randomAnsweredQuestions)
        .totalQuestions(questions.size())
        .status(attempt.getStatus())
        .build();
  }
}
