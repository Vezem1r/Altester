package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.AnswerDTO;
import com.altester.core.dtos.core_service.attempt.OptionDTO;
import com.altester.core.dtos.core_service.attempt.QuestionDTO;
import com.altester.core.dtos.core_service.attempt.SingleQuestionResponse;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Submission;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestAttemptDTOMapper {

    public QuestionDTO mapQuestionToDTO(Question question) {
        List<OptionDTO> optionDTOs = question.getOptions().stream()
                .map(option -> OptionDTO.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .description(option.getDescription())
                        .build())
                .collect(Collectors.toList());

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
                List<Long> selectedOptionIds = submission.getSelectedOptions() != null
                        ? submission.getSelectedOptions().stream()
                        .map(Option::getId)
                        .collect(Collectors.toList())
                        : new ArrayList<>();

                yield AnswerDTO.builder()
                        .questionId(question.getId())
                        .selectedOptionIds(selectedOptionIds)
                        .build();
            }
            case TEXT_ONLY, TEXT_WITH_IMAGE, IMAGE_ONLY -> AnswerDTO.builder()
                    .questionId(question.getId())
                    .answerText(submission.getAnswerText())
                    .build();
        };
    }

    public SingleQuestionResponse buildSingleQuestionResponse(Attempt attempt, int questionNumber,
                                                              List<Question> questions, Question question,
                                                              AnswerDTO currentAnswer) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = attempt.getStartTime().plusMinutes(attempt.getTest().getDuration());
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

    public SingleQuestionResponse getQuestionByNumber(Attempt attempt, int questionNumber, List<Question> questions) {
        Question question = questions.get(questionNumber - 1);

        AnswerDTO currentAnswer = null;
        if (attempt.getSubmissions() != null) {
            Submission existingSubmission = attempt.getSubmissions().stream()
                    .filter(s -> s.getQuestion().getId() == question.getId())
                    .findFirst()
                    .orElse(null);

            if (existingSubmission != null) {
                currentAnswer = mapSubmissionToAnswerDTO(existingSubmission);
            }
        }

        return buildSingleQuestionResponse(attempt, questionNumber, questions, question, currentAnswer);
    }
}