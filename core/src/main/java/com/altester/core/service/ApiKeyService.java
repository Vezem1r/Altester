package com.altester.core.service;

import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.ApiKeyRequest;
import com.altester.core.dtos.core_service.apiKey.AvailableKeys;
import com.altester.core.util.CacheablePage;

import java.security.Principal;
import java.util.List;

public interface ApiKeyService {

    /**
     * Get all API keys based on user role
     * - Admin: sees all keys
     * - Teacher: sees own keys and global keys
     * @param principal The authenticated user
     * @return Paginated list of API keys
     */
    CacheablePage<ApiKeyDTO> getAll(Principal principal);

    /**
     * Create a new API key
     * @param request The API key creation request
     * @param principal The authenticated user
     */
    void createApiKey(ApiKeyRequest request, Principal principal);

    /**
     * Delete an API key
     * @param id The ID of the API key to delete
     * @param principal The authenticated user
     * @return True if deleted successfully
     */
    boolean deleteApiKey(Long id, Principal principal);

    /**
     * Update an API key
     * @param id The ID of the API key to update
     * @param request The updated API key data
     * @param principal The authenticated user
     */
    void updateApiKey(Long id, ApiKeyRequest request, Principal principal);


    /**
     * Toggle activation status of an API key
     * @param id The ID of the API key to toggle
     * @param principal The authenticated user
     * @return The current active status after toggling
     */
    boolean toggleApiKeyStatus(Long id, Principal principal);

    /**
     * Get all API keys available to a user
     * @param principal The authenticated user
     * @return List of API key DTOs
     */
    List<AvailableKeys> getAvailableApiKeys(Principal principal);
}
