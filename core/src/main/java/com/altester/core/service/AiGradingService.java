package com.altester.core.service;

import com.altester.core.dtos.ai_service.GradingResponse;
import com.altester.core.model.subject.Attempt;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

public interface AiGradingService {

  /**
   * Checks if an attempt is eligible for AI grading and sends it for evaluation if eligible
   *
   * @param attempt The test attempt to evaluate
   */
  @Async
  CompletableFuture<GradingResponse> processAttemptForAiGrading(Attempt attempt);
}
