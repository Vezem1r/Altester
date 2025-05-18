package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestAttemptDTOMapper {

  private final AttemptQuestionService questionService;

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

    AnswerDTO currentAnswer = null;
    if (attempt.getSubmissions() != null) {
      Submission existingSubmission =
          attempt.getSubmissions().stream()
              .filter(s -> s.getQuestion().getId() == question.getId())
              .findFirst()
              .orElse(null);

      if (existingSubmission != null) {
        boolean hasAnswer =
            (existingSubmission.getSelectedOptions() != null
                    && !existingSubmission.getSelectedOptions().isEmpty())
                || (existingSubmission.getAnswerText() != null
                    && !existingSubmission.getAnswerText().isEmpty());

        if (hasAnswer) {
          currentAnswer = mapSubmissionToAnswerDTO(existingSubmission);
        }
      }
    }

    return buildSingleQuestionResponse(attempt, questionNumber, questions, question, currentAnswer);
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
}
