package com.altester.ai_grading_service.util;

import com.altester.ai_grading_service.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("QUESTION", questionText);
    valuesMap.put("MAX_SCORE", String.valueOf(maxScore));
    valuesMap.put("STUDENT_ANSWER", studentAnswer);

    String correctAnswerSection = correctAnswer != null && !correctAnswer.isEmpty()
            ? "Here is the correct answer to guide your evaluation:\n\n" + correctAnswer
            : "No specific correct answer is provided. Use your knowledge to evaluate the student's answer.";

    valuesMap.put("CORRECT_ANSWER_SECTION", correctAnswerSection);

    StringSubstitutor sub = new StringSubstitutor(valuesMap, "{{", "}}");
    return sub.replace(promptTemplate);
  }
}
