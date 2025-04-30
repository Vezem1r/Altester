package com.altester.core.service;

import com.altester.core.model.subject.Attempt;

import java.util.concurrent.CompletableFuture;

public interface AiGradingService {

    /**
     * Checks if an attempt is eligible for AI grading and sends it for evaluation if eligible
     *
     * @param attempt The test attempt to evaluate
     * @return CompletableFuture containing true if the attempt was sent for AI grading, false otherwise
     */
    CompletableFuture<Boolean> processAttemptForAiGrading(Attempt attempt);
}
