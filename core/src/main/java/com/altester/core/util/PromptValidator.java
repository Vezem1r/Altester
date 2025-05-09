package com.altester.core.util;

import com.altester.core.exception.PromptException;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PromptValidator {

  private static final List<String> REQUIRED_PLACEHOLDERS =
      List.of("{{QUESTION}}", "{{CORRECT_ANSWER_SECTION}}", "{{STUDENT_ANSWER}}", "{{MAX_SCORE}}");

  private static final String REQUIRED_FORMAT_SECTION =
      """
                    Please format your response as follows:
                    Score: [number]
                    Feedback: [your detailed feedback explaining the score, what was correct, and what could be improved]""";

  private static final Pattern SQL_INJECTION_PATTERN =
      Pattern.compile(
          ".*(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|TRUNCATE|EXEC)\\s+.*",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern HTML_TAG_PATTERN =
      Pattern.compile(
          "<(?!/?(?:b|i|u|br|p|strong|em|ul|ol|li)\\b)[^>]+>", Pattern.CASE_INSENSITIVE);

  private static final Pattern XSS_PATTERN =
      Pattern.compile(
          ".*(<script|javascript:|vbscript:|data:|onerror=|onload=|eval\\(|alert\\().*",
          Pattern.CASE_INSENSITIVE);

  private final int maxTokenCount;

  public PromptValidator(@Value("${prompt.token.max-count}") int maxTokenCount) {
    this.maxTokenCount = maxTokenCount;
  }

  public void validatePrompt(String prompt) {
    if (prompt == null || prompt.trim().isEmpty()) {
      throw PromptException.invalidPromptContent();
    }

    int tokenCount = estimateTokenCount(prompt);
    if (tokenCount > maxTokenCount) {
      throw PromptException.tokenLimitExceeded(tokenCount, maxTokenCount);
    }

    for (String placeholder : REQUIRED_PLACEHOLDERS) {
      int count = countOccurrences(prompt, placeholder);
      if (count == 0) {
        throw PromptException.invalidPromptTemplate();
      }
      if (count > 1) {
        throw PromptException.invalidPromptContent();
      }
    }

    if (SQL_INJECTION_PATTERN.matcher(prompt).find() || XSS_PATTERN.matcher(prompt).find()) {
      throw PromptException.invalidPromptContent();
    }
  }

  public String sanitizePrompt(String prompt) {
    if (prompt == null) {
      return null;
    }

    prompt = HTML_TAG_PATTERN.matcher(prompt).replaceAll("");

    prompt = prompt.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

    for (String placeholder : REQUIRED_PLACEHOLDERS) {
      String escaped = placeholder.replace("{{", "&lt;&lt;").replace("}}", "&gt;&gt;");
      prompt = prompt.replace(escaped, placeholder);
    }

    if (!prompt.contains(REQUIRED_FORMAT_SECTION)) {
      prompt = prompt.trim() + "\n\n" + REQUIRED_FORMAT_SECTION;
    }

    return prompt.trim();
  }

  private int countOccurrences(String text, String substring) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(substring, index)) != -1) {
      count++;
      index += substring.length();
    }
    return count;
  }

  private int estimateTokenCount(String prompt) {
    if (prompt == null || prompt.trim().isEmpty()) {
      return 0;
    }

    String[] tokens = prompt.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})");

    int count = 0;
    for (String token : tokens) {
      if (!token.trim().isEmpty()) {
        count++;

        if (token.matches("\\d+")) {
          count += token.length() / 3;
        }

        if (token.contains("{{") || token.contains("}}")) {
          count += 2;
        }
      }
    }

    return count;
  }
}
