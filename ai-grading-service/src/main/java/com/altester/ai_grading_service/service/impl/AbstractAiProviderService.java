package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.model.Option;
import com.altester.ai_grading_service.model.Question;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.service.AiProviderService;
import com.altester.ai_grading_service.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAiProviderService implements AiProviderService {

    protected final PromptBuilder promptBuilder;

    @Override
    public GradingResult evaluateSubmission(Submission submission, Question question, String apiKey) {
        try {
            String prompt = buildPrompt(submission, question);
            log.debug("Sending prompt to {}: {}", getProviderName(), prompt);

            String response = sendPromptToAi(prompt, apiKey, question.getScore());
            log.debug("Received response from {}: {}", getProviderName(), response);

            return parseGradingResponse(response, question.getScore());
        } catch (Exception e) {
            log.error("Error evaluating submission with {}: {}", getProviderName(), e.getMessage(), e);
            return new GradingResult(0, "Error evaluating submission with " + getProviderName() + ": " + e.getMessage());
        }
    }

    protected abstract String sendPromptToAi(String prompt, String apiKey, int maxScore);

    protected abstract String getProviderName();

    protected String buildPrompt(Submission submission, Question question) {
        StringBuilder studentAnswer = new StringBuilder();

        if (submission.getAnswerText() != null && !submission.getAnswerText().isEmpty()) {
            studentAnswer.append(submission.getAnswerText());
        }

        if (submission.getSelectedOptions() != null && !submission.getSelectedOptions().isEmpty()) {
            if (!studentAnswer.isEmpty()) {
                studentAnswer.append("\n\nSelected options:\n");
            } else {
                studentAnswer.append("Selected options:\n");
            }

            List<Option> selectedOptions = submission.getSelectedOptions();
            for (Option selectedOption : selectedOptions) {
                studentAnswer.append("- ").append(selectedOption.getText()).append("\n");
            }
        }

        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null && !question.getOptions().isEmpty()) {
            correctAnswer = question.getOptions().stream()
                    .filter(Option::isCorrect)
                    .map(Option::getText)
                    .collect(Collectors.joining("\n- ", "- ", ""));
        }

        return promptBuilder.buildGradingPrompt(
                question.getQuestionText(),
                correctAnswer,
                studentAnswer.toString(),
                question.getScore()
        );
    }

    protected GradingResult parseGradingResponse(String response, int maxScore) {
        try {
            String[] lines = response.split("\\n");
            int score = -1;
            StringBuilder feedback = new StringBuilder();
            boolean feedbackStarted = false;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("Score:")) {
                    String scoreStr = line.substring("Score:".length()).trim();
                    try {
                        score = Integer.parseInt(scoreStr);
                    } catch (NumberFormatException e) {
                        if (scoreStr.contains("/")) {
                            String[] parts = scoreStr.split("/");
                            try {
                                score = Integer.parseInt(parts[0].trim());
                            } catch (NumberFormatException ex) {
                                log.warn("Could not parse score from: {}", scoreStr);
                            }
                        }
                    }
                } else if (line.startsWith("Feedback:")) {
                    feedbackStarted = true;
                    String initialFeedback = line.substring("Feedback:".length()).trim();
                    if (!initialFeedback.isEmpty()) {
                        feedback.append(initialFeedback);
                    }
                } else if (feedbackStarted) {
                    if (!line.isEmpty()) {
                        if (!feedback.isEmpty()) {
                            feedback.append("\n");
                        }
                        feedback.append(line);
                    }
                }
            }

            if (score == -1) {
                if (response.toLowerCase().contains("correct") ||
                        response.toLowerCase().contains("perfect") ||
                        response.toLowerCase().contains("excellent")) {
                    score = maxScore;
                } else if (response.toLowerCase().contains("partially") ||
                        response.toLowerCase().contains("almost")) {
                    score = maxScore / 2;
                } else {
                    score = 0;
                }
            }

            if (score < 0) score = 0;
            if (score > maxScore) score = maxScore;

            String feedbackStr = feedback.toString();
            if (feedbackStr.isEmpty()) {
                feedbackStr = "No specific feedback provided.";
            }

            return new GradingResult(score, feedbackStr);
        } catch (Exception e) {
            log.error("Error parsing grading response: {}", e.getMessage(), e);
            return new GradingResult(0, "Error evaluating submission: " + e.getMessage());
        }
    }
}