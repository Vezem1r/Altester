package com.altester.ai_grading_service.service;

public interface PromptService {

  /**
   * Get prompt content by ID
   *
   * @param promptId The ID of the prompt to retrieve
   * @return The prompt content
   */
  String getPromptById(Long promptId);

  /**
   * Get the default prompt content
   *
   * @return The default prompt content
   */
  String getDefaultPrompt();
}
