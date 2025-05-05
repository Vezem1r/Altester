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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGradingServiceImpl implements AiGradingService {

    private final AttemptRepository attemptRepository;
    private final SubmissionService submissionService;
    private final List<AiProviderService> aiProviderServices;

    @Value("${CORE_SERVICE_URL}")
    private String coreServiceUrl;

    @Value("${INTERNAL_API_KEY}")
    private String internalApiKey;

    private final RestTemplate restTemplate;

    @Override
    public GradingResponse gradeAttempt(GradingRequest request) {
        log.info("Processing grading request for attempt: {}, using AI service: {}, with prompt ID: {}",
                request.getAttemptId(), request.getAiServiceName(), request.getPromptId());

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

        // Filter out auto-gradable submissions
        List<Submission> submissionsForAiGrading = submissions.stream()
                .filter(submission -> !isAutoGradableQuestion(submission))
                .filter(submission -> !submission.isAiGraded())
                .collect(Collectors.toList());

        if (submissionsForAiGrading.isEmpty()) {
            return GradingResponse.builder()
                    .attemptId(request.getAttemptId())
                    .success(true)
                    .message("All submissions are already graded or auto-gradable")
                    .results(new ArrayList<>())
                    .build();
        }

        try {
            // Process submissions in batches
            List<AiProviderService.GradingResult> gradingResults = provider.evaluateSubmissionsBatch(
                    submissionsForAiGrading,
                    request.getApiKey(),
                    request.getPromptId()
            );

            // Convert results to SubmissionGradingResult
            List<SubmissionGradingResult> submissionResults = new ArrayList<>();
            for (int i = 0; i < submissionsForAiGrading.size() && i < gradingResults.size(); i++) {
                Submission submission = submissionsForAiGrading.get(i);
                AiProviderService.GradingResult result = gradingResults.get(i);

                submissionResults.add(SubmissionGradingResult.builder()
                        .submissionId(submission.getId())
                        .score(result.score())
                        .feedback(result.feedback())
                        .graded(true)
                        .build());
            }

            int savedCount = submissionService.saveGradingResults(submissionResults, attempt);

            return GradingResponse.builder()
                    .attemptId(request.getAttemptId())
                    .success(true)
                    .message(String.format("Successfully graded %d out of %d submissions",
                            savedCount, submissionsForAiGrading.size()))
                    .results(submissionResults)
                    .build();

        } catch (Exception e) {
            log.error("Error processing AI grading for attempt {}: {}", request.getAttemptId(), e.getMessage(), e);
            throw new AiServiceException("Failed to complete grading tasks: " + e.getMessage(), e);
        }
    }

    private boolean isAutoGradableQuestion(Submission submission) {
        QuestionType questionType = submission.getQuestion().getQuestionType();
        return QuestionType.MULTIPLE_CHOICE.equals(questionType) ||
                QuestionType.IMAGE_WITH_MULTIPLE_CHOICE.equals(questionType);
    }

    @Override
    @Transactional
    public GradingResponse gradeAndNotify(GradingRequest request) {
        GradingResponse response = gradeAttempt(request);

        if (response.isSuccess()) {
            notifyCoreServiceGradingComplete(request.getAttemptId());
        }

        return response;
    }

    private void notifyCoreServiceGradingComplete(Long attemptId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", internalApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = coreServiceUrl + "/internal/ai-grading/complete/" + attemptId;

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully notified core service about completion of grading for attempt {}", attemptId);
            } else {
                log.error("Failed to notify core service about completion for attempt {}. Status: {}",
                        attemptId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error notifying core service about completion for attempt {}: {}",
                    attemptId, e.getMessage(), e);
        }
    }
}