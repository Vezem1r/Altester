package com.altester.core.service;

import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.exception.*;
import java.security.Principal;

public interface QuestionService {

  /**
   * Retrieves detailed information for a specific question.
   *
   * @param questionId ID of the question to retrieve
   * @param principal The authenticated user (admin or teacher)
   * @return Question details including options and image path
   * @throws ResourceNotFoundException if the question or user doesn't exist
   * @throws AccessDeniedException if the user doesn't have permission to access the test
   */
  QuestionDetailsDTO getQuestion(Long questionId, Principal principal);
}
