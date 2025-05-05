package com.altester.ai_grading_service.controller;

import com.altester.ai_grading_service.dto.GradingRequest;
import com.altester.ai_grading_service.dto.GradingResponse;
import com.altester.ai_grading_service.service.AiGradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiGradingController {

    private final AiGradingService aiGradingService;

    @PostMapping("/grade")
    public ResponseEntity<GradingResponse> gradeAttempt(@Valid @RequestBody GradingRequest request) {
        log.info("Received grading request for attempt: {}", request.getAttemptId());
        GradingResponse response = aiGradingService.gradeAndNotify(request);
        return ResponseEntity.ok(response);
    }
}