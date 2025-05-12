package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.dto.GradingRequest;
import com.altester.ai_grading_service.dto.GradingResponse;
import com.altester.ai_grading_service.dto.SubmissionGradingResult;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.exception.ResourceNotFoundException;
import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.model.enums.QuestionType;
import com.altester.ai_grading_service.repository.AttemptRepository;
import com.altester.ai_grading_service.service.AiGradingService;
import com.altester.ai_grading_service.service.AiProviderService;
import com.altester.ai_grading_service.service.SubmissionService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGradingServiceImpl implements AiGradingService {

  private final AttemptRepository attemptRepository;
  private final SubmissionService submissionService;
  private final List<AiProviderService> aiProviderServices;

  @Override
  public GradingResponse gradeAttempt(GradingRequest request) {
    log.info(
        "Processing grading request for attempt: {}, using AI service: {}, with prompt ID: {}",
        request.getAttemptId(),
        request.getAiServiceName(),
        request.getPromptId());

    Attempt attempt =
        attemptRepository
            .findById(request.getAttemptId())
            .orElseThrow(() -> ResourceNotFoundException.attempt(request.getAttemptId()));

    AiProviderService provider =
        aiProviderServices.stream()
            .filter(p -> p.supports(request.getAiServiceName()))
            .findFirst()
            .orElseThrow(
                () ->
                    new AiServiceException(
                        "Unsupported AI service: " + request.getAiServiceName()));

    List<Submission> submissions = submissionService.getSubmissionsForAiGrading(attempt);

    if (submissions.isEmpty()) {
      return GradingResponse.builder()
          .attemptId(request.getAttemptId())
          .success(false)
          .message("No submissions found that require AI grading")
          .results(new ArrayList<>())
          .build();
    }

    // Filter out auto-gradable submissions
    List<Submission> submissionsForAiGrading =
        submissions.stream()
            .filter(submission -> !isAutoGradableQuestion(submission))
            .filter(submission -> !submission.isAiGraded())
            .toList();

    if (submissionsForAiGrading.isEmpty()) {
      return GradingResponse.builder()
          .attemptId(request.getAttemptId())
          .success(false)
          .message("All submissions are already graded or auto-gradable")
          .results(new ArrayList<>())
          .build();
    }

    try {
      // Process submissions in batches
      List<AiProviderService.GradingResult> gradingResults =
          provider.evaluateSubmissionsBatch(
              submissionsForAiGrading,
              request.getApiKey(),
              request.getModel(),
              request.getPromptId());

      int totalScore = 0;

      // Convert results to SubmissionGradingResult
      List<SubmissionGradingResult> submissionResults = new ArrayList<>();
      for (int i = 0; i < submissionsForAiGrading.size() && i < gradingResults.size(); i++) {
        Submission submission = submissionsForAiGrading.get(i);
        AiProviderService.GradingResult result = gradingResults.get(i);

        if (result.score() < 0) {
          log.error(
              "Score is negative {} for submission id {}", result.score(), submission.getId());
          continue;
        }

        submissionResults.add(
            SubmissionGradingResult.builder()
                .submissionId(submission.getId())
                .score(result.score())
                .feedback(result.feedback())
                .graded(true)
                .build());

        totalScore += result.score();
      }

      int savedCount = submissionService.saveGradingResults(submissionResults, attempt);

      return GradingResponse.builder()
          .attemptId(request.getAttemptId())
          .success(true)
          .attemptScore(totalScore)
          .message(
              String.format(
                  "Successfully graded %d out of %d submissions",
                  savedCount, submissionsForAiGrading.size()))
          .results(submissionResults)
          .build();

    } catch (Exception e) {
      log.error(
          "Error processing AI grading for attempt {}: {}",
          request.getAttemptId(),
          e.getMessage(),
          e);
      throw new AiServiceException("Failed to complete grading tasks: " + e.getMessage(), e);
    }
  }

  private boolean isAutoGradableQuestion(Submission submission) {
    QuestionType questionType = submission.getQuestion().getQuestionType();
    return QuestionType.MULTIPLE_CHOICE.equals(questionType)
        || QuestionType.IMAGE_WITH_MULTIPLE_CHOICE.equals(questionType);
  }
}
