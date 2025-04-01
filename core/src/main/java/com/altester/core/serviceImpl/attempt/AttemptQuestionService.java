package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.AnswerDTO;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Submission;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionType;
import com.altester.core.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptQuestionService {

    private final OptionRepository optionRepository;

    /**
     * Retrieves and potentially randomizes questions for a test.
     */
    public List<Question> getQuestionsForTest(Test test) {
        List<Question> allQuestions = new ArrayList<>(test.getQuestions());
        allQuestions.sort(Comparator.comparing(Question::getPosition));

        Integer maxQuestions = test.getMaxQuestions();
        if (maxQuestions != null && maxQuestions < allQuestions.size()) {
            Collections.shuffle(allQuestions);
            return allQuestions.subList(0, maxQuestions);
        }

        return allQuestions;
    }

    /**
     * Identifies the next unanswered question in an ongoing test attempt.
     */
    public int findQuestionToResume(Attempt attempt, List<Question> questions) {
        if (attempt.getSubmissions() == null || attempt.getSubmissions().isEmpty()) {
            return 1;
        }

        Set<Long> answeredQuestionIds = attempt.getSubmissions().stream()
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
        return questions.stream()
                .map(Question::getId)
                .toList()
                .indexOf(question.getId());
    }

    public void updateSubmission(Submission submission, AnswerDTO answerDTO, Question question) {
        submission.setSelectedOptions(new ArrayList<>());
        submission.setAnswerText(null);

        QuestionType questionType = question.getQuestionType();

        if (questionType == QuestionType.MULTIPLE_CHOICE || questionType == QuestionType.IMAGE_WITH_MULTIPLE_CHOICE) {
            processMultipleChoiceAnswer(submission, answerDTO);
        } else {
            submission.setAnswerText(answerDTO.getAnswerText());
        }
    }

    private void processMultipleChoiceAnswer(Submission submission, AnswerDTO answerDTO) {
        if (answerDTO.getSelectedOptionIds() == null || answerDTO.getSelectedOptionIds().isEmpty()) {
            return;
        }

        List<Option> selectedOptions = answerDTO.getSelectedOptionIds().stream()
                .map(this::findOptionById)
                .toList();

        submission.getSelectedOptions().addAll(selectedOptions);
    }

    private Option findOptionById(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new com.altester.core.exception.ResourceNotFoundException("Option", optionId.toString(), null));
    }
}