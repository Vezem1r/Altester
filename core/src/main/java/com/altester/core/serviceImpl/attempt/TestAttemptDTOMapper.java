package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestAttemptDTOMapper {

  private final Random random = new Random();

  public QuestionDTO mapQuestionToDTO(Question question) {
    List<OptionDTO> optionDTOs = new ArrayList<>();

    try {
      if (question.getOptions() != null && !question.getOptions().isEmpty()) {
        optionDTOs = question.getOptions().stream()
                .map(option ->
                        OptionDTO.builder()
                                .id(option.getId())
                                .text(option.getText())
                                .description(option.getDescription())
                                .build())
                .toList();
      }
    } catch (Exception e) {
      log.warn("[DEMO MODE] Could not load options for question {}: {}", question.getId(), e.getMessage());
      optionDTOs = new ArrayList<>();
    }

    return QuestionDTO.builder()
            .id(question.getId())
            .questionText(question.getQuestionText())
            .imagePath(question.getImagePath())
            .score(question.getScore())
            .questionType(question.getQuestionType())
            .options(optionDTOs)
            .build();
  }

  public SingleQuestionResponse buildDemoQuestionResponse(
          Test test, User student, int questionNumber, List<Question> questions) {

    Question question = questions.get(questionNumber - 1);
    AnswerDTO currentAnswer = generateRandomAnswer(question);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(random.nextInt(30) + 5);
    LocalDateTime expirationTime = startTime.plusMinutes(test.getDuration());
    int timeRemainingSeconds = (int) Duration.between(now, expirationTime).getSeconds();
    if (timeRemainingSeconds < 0) timeRemainingSeconds = 0;

    return SingleQuestionResponse.builder()
            .attemptId(test.getId())
            .testTitle(test.getTitle() + " (DEMO)")
            .testDescription(test.getDescription())
            .duration(test.getDuration())
            .startTime(startTime)
            .endTime(expirationTime)
            .totalQuestions(questions.size())
            .currentQuestionNumber(questionNumber)
            .question(mapQuestionToDTO(question))
            .currentAnswer(currentAnswer)
            .isCompleted(false)
            .isExpired(now.isAfter(expirationTime))
            .timeRemainingSeconds(timeRemainingSeconds)
            .build();
  }

  private AnswerDTO generateRandomAnswer(Question question) {
    if (random.nextBoolean()) {
      return null;
    }

    QuestionType questionType = question.getQuestionType();

    return switch (questionType) {
      case MULTIPLE_CHOICE, IMAGE_WITH_MULTIPLE_CHOICE -> {
        try {
          if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            List<Long> selectedIds = new ArrayList<>();
            Option randomOption = question.getOptions().get(random.nextInt(question.getOptions().size()));
            selectedIds.add(randomOption.getId());

            yield AnswerDTO.builder()
                    .questionId(question.getId())
                    .selectedOptionIds(selectedIds)
                    .build();
          }
        } catch (Exception e) {
          log.warn("[DEMO MODE] Could not generate random answer for question {}: {}", question.getId(), e.getMessage());
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

  public AttemptResultResponse buildDemoAttemptResult(Test test, User student, List<Question> questions, int aiScore) {
    int randomAnsweredQuestions = random.nextInt(questions.size()) + 1;

    return AttemptResultResponse.builder()
            .attemptId(test.getId())
            .testTitle(test.getTitle() + " (DEMO)")
            .score(aiScore)
            .totalScore(test.getTotalScore())
            .questionsAnswered(randomAnsweredQuestions)
            .totalQuestions(questions.size())
            .status(AttemptStatus.AI_REVIEWED)
            .build();
  }
}