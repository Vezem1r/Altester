package com.altester.ai_grading_service.util;

import com.altester.ai_grading_service.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PromptBuilder {

  private final PromptService promptService;

  public String buildGradingPrompt(
      String questionText,
      String correctAnswer,
      String studentAnswer,
      int maxScore,
      Long promptId) {
    String promptTemplate = promptService.getPromptById(promptId);
    return buildPromptFromTemplate(
        promptTemplate, questionText, correctAnswer, studentAnswer, maxScore);
  }

  private String buildPromptFromTemplate(
      String promptTemplate,
      String questionText,
      String correctAnswer,
      String studentAnswer,
      int maxScore) {
    String prompt = promptTemplate;

    prompt = prompt.replace("{{QUESTION}}", questionText);
    prompt = prompt.replace("{{MAX_SCORE}}", String.valueOf(maxScore));
    prompt = prompt.replace("{{STUDENT_ANSWER}}", studentAnswer);

    if (correctAnswer != null && !correctAnswer.isEmpty()) {
      prompt =
          prompt.replace(
              "{{CORRECT_ANSWER_SECTION}}",
              "Here is the correct answer to guide your evaluation:\n\n" + correctAnswer);
    } else {
      prompt =
          prompt.replace(
              "{{CORRECT_ANSWER_SECTION}}",
              "No specific correct answer is provided. Use your knowledge to evaluate the student's answer.");
    }

    return prompt;
  }
}
