package com.altester.core.serviceImpl.aigrading;

import com.altester.core.dtos.ai_service.GradingRequest;
import com.altester.core.exception.ApiKeyException;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.TestGroupAssignmentRepository;
import com.altester.core.service.AiGradingService;
import com.altester.core.util.ApiKeyEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ApiKeyEncryptionUtil apiKeyEncryptionUtil;
    private final ApiKeyEncryptionUtil encryptionUtil;

    @Value("${AI_SERVICE_URL}")
    private String aiGradingServiceUrl;

    @Override
    @Async
    public CompletableFuture<Boolean> processAttemptForAiGrading(Attempt attempt) {
        log.info("Checking if attempt {} is eligible for AI grading", attempt.getId());

        try {
            Optional<ApiKey> apiKeyOpt = findApiKeyForEvaluation(attempt);
            if (apiKeyOpt.isEmpty()) {
                log.debug("No AI evaluation configured for attempt {}", attempt.getId());
                return CompletableFuture.completedFuture(false);
            }

            ApiKey apiKey = apiKeyOpt.get();
            sendAttemptForGrading(attempt, apiKey);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
                log.error("Failed to send attempt {} for AI grading: {}", attempt.getId(), e.getMessage(), e);
                return CompletableFuture.completedFuture(false);
        }
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
     * Sends an attempt for AI grading
     *
     * @param attempt The attempt to be graded
     * @param apiKey The API key to use for grading
     */
    private void sendAttemptForGrading(Attempt attempt, ApiKey apiKey) {
        log.info("Sending attempt {} for AI grading with service: {}",
                attempt.getId(), apiKey.getAiServiceName());

        try {
            String decryptedKey = encryptionUtil.decrypt(apiKey.getEncryptedKey());

            Long promptId = findPromptForAttempt(attempt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            GradingRequest request = GradingRequest.builder()
                    .attemptId(attempt.getId())
                    .apiKey(decryptedKey)
                    .aiServiceName(apiKey.getAiServiceName())
                    .promptId(promptId)
                    .build();

            HttpEntity<GradingRequest> entity = new HttpEntity<>(request, headers);

            String endpoint = aiGradingServiceUrl + "/ai/grade";
            restTemplate.postForEntity(endpoint, entity, Void.class);

            log.info("AI grading request for attempt {} sent successfully", attempt.getId());
        } catch (RestClientException e) {
            log.error("REST client error when sending attempt {} for AI grading: {}",
                    attempt.getId(), e.getMessage(), e);
            throw e;
        } catch (ApiKeyException e) {
            log.error("API key decryption error for attempt {}: {}",
                    attempt.getId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending attempt {} for AI grading: {}",
                    attempt.getId(), e.getMessage(), e);
            throw e;
        }
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
