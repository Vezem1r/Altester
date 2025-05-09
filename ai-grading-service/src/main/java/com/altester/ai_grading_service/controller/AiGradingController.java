package com.altester.ai_grading_service.controller;

import com.altester.ai_grading_service.dto.GradingRequest;
import com.altester.ai_grading_service.dto.GradingResponse;
import com.altester.ai_grading_service.service.AiGradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    try {
      GradingResponse response = aiGradingService.gradeAttempt(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error processing synchronous grading request: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GradingResponse.builder()
                  .attemptId(request.getAttemptId())
                  .success(false)
                  .message("Error processing grading request: " + e.getMessage())
                  .build());
    }
  }
}
