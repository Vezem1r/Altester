package com.altester.ai_grading_service.service;

import com.altester.ai_grading_service.dto.GradingRequest;
import com.altester.ai_grading_service.dto.GradingResponse;

public interface AiGradingService {

  /**
   * Process an attempt for AI grading
   *
   * @param request The grading request containing attempt ID and AI service details
   * @return GradingResponse containing the results of the grading process
   */
  GradingResponse gradeAttempt(GradingRequest request);
}
