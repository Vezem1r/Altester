package com.altester.core.service;

import com.altester.core.dtos.core_service.apiKey.*;
import com.altester.core.exception.*;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import java.util.List;

public interface ApiKeyService {

  /**
   * Get all API keys based on user role - Admin: sees all keys - Teacher: sees only their own keys
   * and global keys
   *
   * @param principal The authenticated user
   * @return Paginated list of API keys visible to the user
   * @throws ResourceNotFoundException if the user doesn't exist
   */
  CacheablePage<ApiKeyDTO> getAll(Principal principal);

  /**
   * Get all active API keys available to a user - Admin: sees all active API keys - Teacher: sees
   * active global keys and their own active keys
   *
   * @param principal The authenticated user
   * @return List of available API key DTOs
   * @throws ResourceNotFoundException if the user doesn't exist
   */
  List<AvailableKeys> getAvailableApiKeys(Principal principal);

  /**
   * Retrieves a list of all API keys assigned to a test, including group and teacher information -
   * Admin: can see all API key assignments for the test - Teacher: can only see API key assignments
   * for groups where they are the teacher
   *
   * @param testId The ID of the test
   * @param principal The authenticated user
   * @return DTO containing list of API key assignments with group and teacher details
   * @throws ResourceNotFoundException if the test doesn't exist
   * @throws AccessDeniedException if a teacher has no groups associated with the test
   */
  TestApiKeysDTO getTestApiKeys(Long testId, Principal principal);
}
