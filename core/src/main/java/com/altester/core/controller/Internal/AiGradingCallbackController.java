package com.altester.core.controller.Internal;

import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.subject.Attempt;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.SubmissionRepository;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ai-grading")
@RequiredArgsConstructor
@Slf4j
public class AiGradingCallbackController {

    private final AttemptRepository attemptRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final CacheService cacheService;
    private final SubmissionRepository submissionRepository;

    @Value("${INTERNAL_API_KEY}")
    private String internalApiKey;

    @PostMapping("/complete/{attemptId}")
    public ResponseEntity<Void> handleAiGradingComplete(
            @PathVariable Long attemptId,
            @RequestHeader("x-api-key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Received AI grading completion for attempt: {}", attemptId);

        try {
            Attempt attempt = attemptRepository.findByIdWithSubmissions(attemptId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

            notificationDispatchService.notifyTestGraded(attempt);

            cacheService.clearAttemptRelatedCaches();
            cacheService.clearStudentRelatedCaches();

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling AI grading completion for attempt {}: {}", attemptId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
