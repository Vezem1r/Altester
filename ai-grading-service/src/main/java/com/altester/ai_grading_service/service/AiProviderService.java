package com.altester.ai_grading_service.service;

import com.altester.ai_grading_service.model.Question;
import com.altester.ai_grading_service.model.Submission;
import java.util.List;

public interface AiProviderService {

  /**
   * Evaluates a student submission using the AI provider
   *
   * @param submission The student's submission
   * @param question The question
   * @param apiKey The API key for the AI service
   * @return GradingResult containing score and feedback
   */
  GradingResult evaluateSubmission(
      Submission submission, Question question, String apiKey, String model, Long promptId);

  List<GradingResult> evaluateSubmissionsBatch(
      List<Submission> submissions, String apiKey, String model, Long promptId);

  /**
   * Checks if the AI provider is supported
   *
   * @param providerName The name of the AI provider
   * @return true if supported, false otherwise
   */
  boolean supports(String providerName);

  /** Container class for the grading result */
  record GradingResult(int score, String feedback) {}
}
