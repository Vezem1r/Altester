package com.altester.ai_grading_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PromptBuilder {

    private final String gradingPromptTemplate;

    public PromptBuilder(@Value("classpath:prompts/grading_prompt.txt") Resource gradingPromptResource) {
        try (Reader reader = new InputStreamReader(gradingPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            this.gradingPromptTemplate = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error("Failed to load grading prompt template", e);
            throw new RuntimeException("Could not load grading prompt template", e);
        }
    }

    public String buildGradingPrompt(String questionText, String correctAnswer, String studentAnswer, int maxScore) {
        String prompt = gradingPromptTemplate;

        prompt = prompt.replace("{{QUESTION}}", questionText);
        prompt = prompt.replace("{{MAX_SCORE}}", String.valueOf(maxScore));
        prompt = prompt.replace("{{STUDENT_ANSWER}}", studentAnswer);

        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            prompt = prompt.replace("{{CORRECT_ANSWER_SECTION}}",
                    "Here is the correct answer to guide your evaluation:\n\n" + correctAnswer);
        } else {
            prompt = prompt.replace("{{CORRECT_ANSWER_SECTION}}",
                    "No specific correct answer is provided. Use your knowledge to evaluate the student's answer.");
        }

        return prompt;
    }
}
