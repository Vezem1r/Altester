package com.altester.core.serviceImpl.attemptRetrieval;

import com.altester.core.dtos.core_service.student.AttemptReviewDTO;
import com.altester.core.dtos.core_service.student.OptionReviewDTO;
import com.altester.core.dtos.core_service.student.QuestionReviewDTO;
import com.altester.core.model.subject.*;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptReviewService {
  private final AttemptRepository attemptRepository;
  private final NotificationDispatchService notificationService;
  private final CacheService cacheService;

  public AttemptReviewDTO createAttemptReviewDTO(Attempt attempt) {
    Test test = attempt.getTest();

    List<QuestionReviewDTO> questionReviews = new ArrayList<>();
    for (Submission submission : attempt.getSubmissions()) {
      Question question = submission.getQuestion();

      List<OptionReviewDTO> optionReviews = new ArrayList<>();
      for (Option option : question.getOptions()) {
        boolean isSelected = submission.getSelectedOptions().contains(option);

        optionReviews.add(
            OptionReviewDTO.builder()
                .optionId(option.getId())
                .text(option.getText())
                .description(option.getDescription())
                .isSelected(isSelected)
                .isCorrect(option.isCorrect())
                .build());
      }

      List<Long> selectedOptionIds =
          submission.getSelectedOptions().stream().map(Option::getId).toList();

      questionReviews.add(
          QuestionReviewDTO.builder()
              .submissionId(submission.getId())
              .questionText(question.getQuestionText())
              .imagePath(question.getImagePath())
              .options(optionReviews)
              .studentAnswer(submission.getAnswerText())
              .selectedOptionIds(selectedOptionIds)
              .score(submission.getScore() != null ? submission.getScore() : 0)
              .aiScore(submission.getAiScore() != null ? submission.getAiScore() : 0)
              .maxScore(question.getScore())
              .teacherFeedback(submission.getTeacherFeedback())
              .aiFeedback(submission.getAiFeedback())
              .build());
    }

    return AttemptReviewDTO.builder()
        .attemptId(attempt.getId())
        .testTitle(test.getTitle())
        .testDescription(test.getDescription())
        .score(attempt.getScore() != null ? attempt.getScore() : 0)
        .aiScore(attempt.getAiScore() != null ? attempt.getAiScore() : 0)
        .totalScore(test.getTotalScore())
        .startTime(attempt.getStartTime())
        .endTime(attempt.getEndTime())
        .questions(questionReviews)
        .build();
  }
}
