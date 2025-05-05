package com.altester.core.serviceImpl.aigrading;

import com.altester.core.dtos.ai_service.GradingRequest;
import com.altester.core.dtos.ai_service.GradingResponse;
import com.altester.core.exception.ApiKeyException;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.TestGroupAssignmentRepository;
import com.altester.core.service.AiGradingService;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.ApiKeyEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiGradingServiceImpl implements AiGradingService {

    private final GroupRepository groupRepository;
    private final TestGroupAssignmentRepository assignmentRepository;
    private final RestTemplate restTemplate;
    private final ApiKeyEncryptionUtil encryptionUtil;
    private final AttemptRepository attemptRepository;
    private final CacheService cacheService;
    private final NotificationDispatchService notificationDispatchService;

    @Value("${INTERNAL_API_KEY}")
    private String internalApiKey;

    @Value("${AI_SERVICE_URL}")
    private String aiGradingServiceUrl;

    @Override
    @Async
    public CompletableFuture<GradingResponse> processAttemptForAiGrading(Attempt attempt) {
        log.info("Checking if attempt {} is eligible for AI grading", attempt.getId());
        CompletableFuture<GradingResponse> future = new CompletableFuture<>();

        try {
            Optional<ApiKey> apiKeyOpt = findApiKeyForEvaluation(attempt);
            if (apiKeyOpt.isEmpty()) {
                log.debug("No AI evaluation configured for attempt {}", attempt.getId());
                future.complete(null);
                return future;
            }

            ApiKey apiKey = apiKeyOpt.get();
            Long promptId = findPromptForAttempt(attempt);

            return sendAttemptForGrading(attempt, apiKey, promptId);
        } catch (Exception e) {
                log.error("Failed to send attempt {} for AI grading: {}", attempt.getId(), e.getMessage(), e);
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Finds an API key for AI evaluation based on the test group assignment
     *
     * @param attempt The attempt to check for AI evaluation
     * @return An optional API key if AI evaluation is enabled
     */
    private Optional<ApiKey> findApiKeyForEvaluation(Attempt attempt) {
        Test test = attempt.getTest();

        List<Group> studentGroups = groupRepository.findAllByStudentsContaining(attempt.getStudent());

        for (Group group : studentGroups) {
            Optional<TestGroupAssignment> assignment = assignmentRepository.findByTestAndGroup(test, group);

            if (assignment.isPresent() &&
                    assignment.get().isAiEvaluation() &&
                    assignment.get().getApiKey() != null) {

                ApiKey apiKey = assignment.get().getApiKey();

                if (apiKey.isActive()) {
                    log.info("Found active API key {} for AI evaluation of attempt {}",
                            apiKey.getId(), attempt.getId());
                    return Optional.of(apiKey);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Sends an attempt for AI grading and waits for response
     *
     * @param attempt  The attempt to be graded
     * @param apiKey   The API key to use for grading
     * @param promptId The prompt ID to use for grading
     */
    private CompletableFuture<GradingResponse> sendAttemptForGrading(Attempt attempt, ApiKey apiKey, Long promptId) {
        log.info("Sending attempt {} for AI grading with service: {}",
                attempt.getId(), apiKey.getAiServiceName());

        CompletableFuture<GradingResponse> future = new CompletableFuture<>();

        try {
            HttpEntity<GradingRequest> requestEntity = prepareGradingRequest(attempt, apiKey, promptId);
            sendGradingRequest(attempt, requestEntity, future);
        } catch (Exception e) {
            handlePreparationError(attempt, e, future);
        }

        return future;
    }

    private HttpEntity<GradingRequest> prepareGradingRequest(Attempt attempt, ApiKey apiKey, Long promptId)
            throws ApiKeyException {
        String decryptedKey = encryptionUtil.decrypt(apiKey.getEncryptedKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GradingRequest request = GradingRequest.builder()
                .attemptId(attempt.getId())
                .apiKey(decryptedKey)
                .aiServiceName(apiKey.getAiServiceName())
                .model(apiKey.getModel())
                .promptId(promptId)
                .build();

        return new HttpEntity<>(request, headers);
    }

    private void sendGradingRequest(Attempt attempt, HttpEntity<GradingRequest> entity,
                                    CompletableFuture<GradingResponse> future) {
        String endpoint = aiGradingServiceUrl + "/ai/grade";

        try {
            ResponseEntity<GradingResponse> response = restTemplate.postForEntity(
                    endpoint, entity, GradingResponse.class);
            handleGradingResponse(attempt, response, future);
        } catch (RestClientException e) {
            handleRestClientError(attempt, e, future);
        } catch (Exception e) {
            handleUnexpectedError(attempt, e, future);
        }
    }

    private void handleGradingResponse(Attempt attempt, ResponseEntity<GradingResponse> response,
                                       CompletableFuture<GradingResponse> future) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("AI grading for attempt {} completed successfully with score: {}",
                    attempt.getId(), response.getBody().getAttemptScore());
            future.complete(response.getBody());
        } else {
            log.error("AI grading for attempt {} failed with status: {}",
                    attempt.getId(), response.getStatusCode());
            future.complete(null);
        }
    }

    private void handleRestClientError(Attempt attempt, RestClientException e,
                                       CompletableFuture<GradingResponse> future) {
        log.error("REST client error when sending attempt {} for AI grading: {}",
                attempt.getId(), e.getMessage(), e);
        future.completeExceptionally(e);
    }

    private void handleUnexpectedError(Attempt attempt, Exception e,
                                       CompletableFuture<GradingResponse> future) {
        log.error("Unexpected error sending attempt {} for AI grading: {}",
                attempt.getId(), e.getMessage(), e);
        future.completeExceptionally(e);
    }

    private void handlePreparationError(Attempt attempt, Exception e,
                                        CompletableFuture<GradingResponse> future) {
        if (e instanceof ApiKeyException) {
            log.error("API key decryption error for attempt {}: {}",
                    attempt.getId(), e.getMessage(), e);
        } else {
            log.error("Unexpected error preparing request for attempt {}: {}",
                    attempt.getId(), e.getMessage(), e);
        }
        future.completeExceptionally(e);
    }

    private Long findPromptForAttempt(Attempt attempt) {
        Test test = attempt.getTest();
        List<Group> studentGroups = groupRepository.findAllByStudentsContaining(attempt.getStudent());

        for (Group group : studentGroups) {
            Optional<TestGroupAssignment> assignment = assignmentRepository.findByTestAndGroup(test, group);

            if (assignment.isPresent() && assignment.get().getPrompt() != null) {
                return assignment.get().getPrompt().getId();
            }
        }

        log.info("No prompt found for attempt {}, using default prompt ID 1", attempt.getId());
        return 1L;
    }
}
