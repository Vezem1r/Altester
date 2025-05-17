package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.exception.AiApiServiceException;
import com.altester.ai_grading_service.model.Option;
import com.altester.ai_grading_service.model.Question;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.service.AiProviderService;
import com.altester.ai_grading_service.util.PromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAiProviderService implements AiProviderService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  protected final PromptBuilder promptBuilder;

  @Override
  public List<GradingResult> evaluateSubmissionsBatch(
      List<Submission> submissions, String apiKey, String model, Long promptId)
      throws AiApiServiceException {
    int batchSize = 5;
    List<GradingResult> allResults = new ArrayList<>();

    for (int i = 0; i < submissions.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, submissions.size());
      List<Submission> batch = submissions.subList(i, endIndex);

      try {
        String batchPrompt = buildBatchPrompt(batch, promptId);
        log.debug("Sending batch prompt to {}: {} questions", getProviderName(), batch.size());

        String response =
            sendPromptToAi(batchPrompt, apiKey, model, calculateMaxScoreForBatch(batch));
        log.debug("Received batch response from {}", getProviderName());

        List<GradingResult> batchResults = parseBatchGradingResponse(response, batch);
        allResults.addAll(batchResults);
      } catch (Exception e) {
        log.error("Error evaluating batch with {}: {}", getProviderName(), e.getMessage(), e);
        for (Submission submission : batch) {
          allResults.add(
              evaluateSubmission(submission, submission.getQuestion(), apiKey, model, promptId));
        }
      }
    }

    return allResults;
  }

  @Override
  public GradingResult evaluateSubmission(
      Submission submission, Question question, String apiKey, String model, Long promptId)
      throws AiApiServiceException {
    try {
      String prompt = buildPrompt(submission, question, promptId);
      log.debug("Sending prompt to {}: {}", getProviderName(), prompt);

      String response = sendPromptToAi(prompt, apiKey, model, question.getScore());
      log.debug("Received response from {}: {}", getProviderName(), response);

      return parseGradingResponse(response, question.getScore());
    } catch (AiApiServiceException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error evaluating submission with {}: {}", getProviderName(), e.getMessage(), e);
      return new GradingResult(
          -1, "Error evaluating submission with " + getProviderName() + ": " + e.getMessage());
    }
  }

  protected abstract String sendPromptToAi(String prompt, String apiKey, String model, int maxScore)
      throws AiApiServiceException;

  protected abstract String getProviderName();

  protected String buildBatchPrompt(List<Submission> submissions, Long promptId) {
    StringBuilder batchPrompt = new StringBuilder();
    batchPrompt.append("Please evaluate the following student submissions.\n\n");
    batchPrompt.append("For each submission, provide the response in this exact format:\n");
    batchPrompt.append("=== Submission ID: [submission_id] ===\n");
    batchPrompt.append("Score: [number]\n");
    batchPrompt.append("Feedback: [your feedback]\n\n");

    for (Submission submission : submissions) {
      batchPrompt.append("=== Submission ID: ").append(submission.getId()).append(" ===\n");
      batchPrompt.append(buildPrompt(submission, submission.getQuestion(), promptId));
      batchPrompt.append("\n\n");
    }

    return batchPrompt.toString();
  }

  protected String buildPrompt(Submission submission, Question question, Long promptId) {
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
      correctAnswer =
          question.getOptions().stream()
              .filter(Option::isCorrect)
              .map(Option::getText)
              .collect(Collectors.joining("\n- ", "- ", ""));
    }

    return promptBuilder.buildGradingPrompt(
        question.getQuestionText(),
        correctAnswer,
        studentAnswer.toString(),
        question.getScore(),
        promptId);
  }

  protected List<GradingResult> parseBatchGradingResponse(
      String response, List<Submission> submissions) {
    List<GradingResult> results = new ArrayList<>();
    String[] sections = response.split("=== Submission ID:");

    for (int i = 1; i < sections.length; i++) {
      String section = sections[i];
      try {
        int idEndIndex = section.indexOf(" ===");
        if (idEndIndex == -1) continue;

        String submissionIdStr = section.substring(0, idEndIndex).trim();
        long submissionId = Long.parseLong(submissionIdStr);

        Submission matchingSubmission =
            submissions.stream().filter(s -> s.getId() == submissionId).findFirst().orElse(null);

        if (matchingSubmission == null) continue;

        String submissionContent = section.substring(idEndIndex + 4);
        GradingResult result =
            parseGradingResponse(submissionContent, matchingSubmission.getQuestion().getScore());
        results.add(result);
      } catch (Exception e) {
        log.error("Error parsing batch response section: {}", e.getMessage());
        results.add(new GradingResult(0, "Error parsing grading response"));
      }
    }

    if (results.size() < submissions.size()) {
      log.warn("Batch response missing some results, filling with error responses");
      while (results.size() < submissions.size()) {
        results.add(new GradingResult(-1, "No grading response received"));
      }
    }

    return results;
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
        } else if (feedbackStarted && !line.isEmpty()) {
          if (!feedback.isEmpty()) {
            feedback.append("\n");
          }
          feedback.append(line);
        }
      }

      if (score == -1) {
        if (response.toLowerCase().contains("correct")
            || response.toLowerCase().contains("perfect")
            || response.toLowerCase().contains("excellent")) {
          score = maxScore;
        } else if (response.toLowerCase().contains("partially")
            || response.toLowerCase().contains("almost")) {
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

  private int calculateMaxScoreForBatch(List<Submission> submissions) {
    return submissions.stream().mapToInt(s -> s.getQuestion().getScore()).sum();
  }

  protected String parseErrorMessage(String errorBody) {
    if (errorBody == null || errorBody.isEmpty()) {
      return "No error details";
    }

    try {
      JsonNode root = objectMapper.readTree(errorBody);

      if (root.has("error")) {
        JsonNode errorNode = root.get("error");
        if (errorNode.has("message")) {
          return errorNode.get("message").asText();
        }
      }

      if (root.has("message")) {
        return root.get("message").asText();
      }

      if (root.has("error_message")) {
        return root.get("error_message").asText();
      }

      return errorBody;
    } catch (Exception e) {
      log.debug("Failed to parse error body: {}", errorBody, e);
      return errorBody;
    }
  }
}
