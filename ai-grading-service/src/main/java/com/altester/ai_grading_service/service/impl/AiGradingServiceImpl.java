package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.dto.GradingRequest;
import com.altester.ai_grading_service.dto.GradingResponse;
import com.altester.ai_grading_service.dto.SubmissionGradingResult;
import com.altester.ai_grading_service.exception.AiServiceException;
import com.altester.ai_grading_service.exception.ResourceNotFoundException;
import com.altester.ai_grading_service.model.Attempt;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.repository.AttemptRepository;
import com.altester.ai_grading_service.service.AiGradingService;
import com.altester.ai_grading_service.service.AiProviderService;
import com.altester.ai_grading_service.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGradingServiceImpl implements AiGradingService {

    private final AttemptRepository attemptRepository;
    private final SubmissionService submissionService;
    private final List<AiProviderService> aiProviderServices;

    @Override
    @Transactional
    public GradingResponse gradeAttempt(GradingRequest request) {
        log.info("Processing grading request for attempt: {}, using AI service: {}",
                request.getAttemptId(), request.getAiServiceName());

        Attempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> ResourceNotFoundException.attempt(request.getAttemptId()));

        AiProviderService provider = aiProviderServices.stream()
                .filter(p -> p.supports(request.getAiServiceName()))
                .findFirst()
                .orElseThrow(() -> new AiServiceException("Unsupported AI service: " + request.getAiServiceName()));

        List<Submission> submissions = submissionService.getSubmissionsForAiGrading(attempt);

        if (submissions.isEmpty()) {
            return GradingResponse.builder()
                    .attemptId(request.getAttemptId())
                    .success(true)
                    .message("No submissions found that require AI grading")
                    .results(new ArrayList<>())
                    .build();
        }

        List<CompletableFuture<SubmissionGradingResult>> futures = new ArrayList<>();

        for (Submission submission : submissions) {
            CompletableFuture<SubmissionGradingResult> future = processSubmissionAsync(
                    submission, provider, request.getApiKey());
            futures.add(future);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.get();

            List<SubmissionGradingResult> results = futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Error getting future result: {}", e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            int savedCount = submissionService.saveGradingResults(results);

            return GradingResponse.builder()
                    .attemptId(request.getAttemptId())
                    .success(true)
                    .message(String.format("Successfully graded %d out of %d submissions",
                            savedCount, submissions.size()))
                    .results(results)
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error waiting for grading tasks to complete: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to complete grading tasks: " + e.getMessage(), e);
        }
    }

    @Async
    protected CompletableFuture<SubmissionGradingResult> processSubmissionAsync(
            Submission submission, AiProviderService provider, String apiKey) {

        try {
            log.info("Processing submission {} for question {}",
                    submission.getId(), submission.getQuestion().getId());

            if (submission.isAiGraded()) {
                return CompletableFuture.completedFuture(
                        SubmissionGradingResult.builder()
                                .submissionId(submission.getId())
                                .graded(false)
                                .feedback("Submission already graded")
                                .build()
                );
            }

            if (isAutoGradableQuestion(submission)) {
                return CompletableFuture.completedFuture(
                        SubmissionGradingResult.builder()
                                .submissionId(submission.getId())
                                .graded(false)
                                .feedback("Question is auto-gradable, skipping AI grading")
                                .build()
                );
            }

            AiProviderService.GradingResult result = provider.evaluateSubmission(
                    submission, submission.getQuestion(), apiKey);

            return CompletableFuture.completedFuture(
                    SubmissionGradingResult.builder()
                            .submissionId(submission.getId())
                            .score(result.score())
                            .feedback(result.feedback())
                            .graded(true)
                            .build()
            );

        } catch (Exception e) {
            log.error("Error processing submission {}: {}", submission.getId(), e.getMessage(), e);

            return CompletableFuture.completedFuture(
                    SubmissionGradingResult.builder()
                            .submissionId(submission.getId())
                            .graded(false)
                            .feedback("Error during AI grading: " + e.getMessage())
                            .build()
            );
        }
    }

    private boolean isAutoGradableQuestion(Submission submission) {
        String questionType = submission.getQuestion().getQuestionType();
        return "MULTIPLE_CHOICE".equals(questionType) ||
                "IMAGE_WITH_MULTIPLE_CHOICE".equals(questionType);
    }
}