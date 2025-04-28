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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptQuestionService {

    private final OptionRepository optionRepository;

    /**
     * Retrieves and randomizes questions for a test based on difficulty settings.
     * If specific counts for each difficulty are set, selects that many questions from each difficulty level.
     * Otherwise, falls back to the maximum questions setting.
     *
     * @param test The test entity containing questions and configuration
     * @return A randomized list of selected questions
     */
    public List<Question> getQuestionsForTest(Test test) {

        Map<QuestionDifficulty, List<Question>> questionsByDifficulty = test.getQuestions().stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));

        List<Question> selectedQuestions = new ArrayList<>();

        boolean hasDifficultyDistribution = (test.getEasyQuestionsCount() != null && test.getEasyQuestionsCount() > 0) ||
                (test.getMediumQuestionsCount() != null && test.getMediumQuestionsCount() > 0) ||
                (test.getHardQuestionsCount() != null && test.getHardQuestionsCount() > 0);

        if (hasDifficultyDistribution) {
            log.debug("Using difficulty distribution for test ID: {}", test.getId());

            // Process EASY questions
            if (test.getEasyQuestionsCount() != null && test.getEasyQuestionsCount() > 0) {
                List<Question> easyQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.EASY, Collections.emptyList());
                log.debug("Found {} easy questions in test", easyQuestions.size());

                if (!easyQuestions.isEmpty()) {
                    List<Question> selectedEasyQuestions = getRandomQuestions(easyQuestions, test.getEasyQuestionsCount());
                    log.debug("Selected {} easy questions out of {} available",
                            selectedEasyQuestions.size(), easyQuestions.size());
                    selectedQuestions.addAll(selectedEasyQuestions);
                } else {
                    log.warn("Test requires {} easy questions but none are available", test.getEasyQuestionsCount());
                }
            }

            // Process MEDIUM questions
            if (test.getMediumQuestionsCount() != null && test.getMediumQuestionsCount() > 0) {
                List<Question> mediumQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.MEDIUM, Collections.emptyList());
                log.debug("Found {} medium questions in test", mediumQuestions.size());

                if (!mediumQuestions.isEmpty()) {
                    List<Question> selectedMediumQuestions = getRandomQuestions(mediumQuestions, test.getMediumQuestionsCount());
                    log.debug("Selected {} medium questions out of {} available",
                            selectedMediumQuestions.size(), mediumQuestions.size());
                    selectedQuestions.addAll(selectedMediumQuestions);
                } else {
                    log.warn("Test requires {} medium questions but none are available", test.getMediumQuestionsCount());
                }
            }

            // Process HARD questions
            if (test.getHardQuestionsCount() != null && test.getHardQuestionsCount() > 0) {
                List<Question> hardQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.HARD, Collections.emptyList());
                log.debug("Found {} hard questions in test", hardQuestions.size());

                if (!hardQuestions.isEmpty()) {
                    List<Question> selectedHardQuestions = getRandomQuestions(hardQuestions, test.getHardQuestionsCount());
                    log.debug("Selected {} hard questions out of {} available",
                            selectedHardQuestions.size(), hardQuestions.size());
                    selectedQuestions.addAll(selectedHardQuestions);
                } else {
                    log.warn("Test requires {} hard questions but none are available", test.getHardQuestionsCount());
                }
            }
        } else {
            log.debug("No difficulty distribution specified for test ID: {}, using all questions", test.getId());
            selectedQuestions.addAll(test.getQuestions());
        }

        Collections.shuffle(selectedQuestions);
        log.info("Total of {} questions selected for test ID: {}", selectedQuestions.size(), test.getId());
        return selectedQuestions;
    }

    /**
     * Selects a random subset of questions from a list.
     * If count is greater than or equal to the available questions, all questions will be selected.
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
            log.debug("Requested {} questions but only {} are available, returning all",
                    count, questions.size());
            return new ArrayList<>(questions);
        }

        List<Question> questionsCopy = new ArrayList<>(questions);
        Collections.shuffle(questionsCopy);
        List<Question> selectedQuestions = questionsCopy.subList(0, count);

        log.debug("Randomly selected {} questions from a pool of {}", count, questions.size());
        return selectedQuestions;
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