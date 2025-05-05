package com.altester.core.controller.Internal;

import com.altester.core.service.AiGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ai-grading")
@RequiredArgsConstructor
@Slf4j
public class AiGradingCallbackController {

    private final AiGradingService aiGradingService;

    @PostMapping("/complete/{attemptId}/{score}")
    public ResponseEntity<Void> handleAiGradingComplete(
            @PathVariable Long attemptId,
            @PathVariable int score,
            @RequestHeader("x-api-key") String apiKey) {
        aiGradingService.processGradingCallback(attemptId, score, apiKey);
        return ResponseEntity.ok().build();
    }
}
