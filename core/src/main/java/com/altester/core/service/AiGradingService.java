package com.altester.core.service;

import com.altester.core.model.subject.Attempt;
import org.springframework.scheduling.annotation.Async;

public interface AiGradingService {

    /**
     * Checks if an attempt is eligible for AI grading and sends it for evaluation if eligible
     *
     * @param attempt The test attempt to evaluate
     */
    @Async
    void processAttemptForAiGrading(Attempt attempt);
}
