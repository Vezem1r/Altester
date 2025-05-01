package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.model.Question;
import com.altester.ai_grading_service.model.Submission;
import com.altester.ai_grading_service.service.AiProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiProviderService  implements AiProviderService {

    @Override
    public GradingResult evaluateSubmission(Submission submission, Question question, String apiKey) {
        return null;
    }

    @Override
    public boolean supports(String providerName) {
        return false;
    }
}
