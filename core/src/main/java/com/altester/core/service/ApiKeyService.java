package com.altester.core.service;

import com.altester.core.dtos.core_service.apiKey.*;
import com.altester.core.util.CacheablePage;
import com.altester.core.exception.*;

import java.security.Principal;
import java.util.List;

public interface ApiKeyService {

    /**
     * Get all API keys based on user role
     * - Admin: sees all keys
     * - Teacher: sees only their own keys and global keys
     *
     * @param principal The authenticated user
     * @return Paginated list of API keys visible to the user
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    CacheablePage<ApiKeyDTO> getAll(Principal principal);

    /**
     * Create a new API key
     * - Admin: can create global or non-global keys
     * - Teacher: can only create non-global keys (owned by themselves)
     *
     * @param request The API key creation request containing name, key value, service name, and global status
     * @param principal The authenticated user
     * @throws AccessDeniedException if a non-admin user attempts to create a global key
     * @throws ApiKeyException if there is an error encrypting the API key
     */
    void createApiKey(ApiKeyRequest request, Principal principal);

    /**
     * Delete an API key
     * - Admin: can delete any API key
     * - Teacher: can only delete their own (non-global) API keys
     *
     * @param id The ID of the API key to delete
     * @param principal The authenticated user
     * @return True if deleted successfully
     * @throws ResourceNotFoundException if the API key doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to delete this key
     */
    boolean deleteApiKey(Long id, Principal principal);

    /**
     * Update an API key's properties
     * - Admin: can update any API key
     * - Teacher: can only update their own keys
     *
     * @param id The ID of the API key to update
     * @param request The updated API key data
     * @param principal The authenticated user
     * @throws ResourceNotFoundException if the API key doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to update this key
     * @throws ApiKeyException if there is an error encrypting the new API key value
     */
    void updateApiKey(Long id, ApiKeyRequest request, Principal principal);

    /**
     * Toggle activation status of an API key
     * - Admin: can toggle any API key
     * - Teacher: can only toggle their own keys
     * - When deactivating, the key is automatically unassigned from all tests
     *
     * @param id The ID of the API key to toggle
     * @param principal The authenticated user
     * @return The current active status after toggling (true=active, false=inactive)
     * @throws ResourceNotFoundException if the API key doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to toggle this key
     */
    boolean toggleApiKeyStatus(Long id, Principal principal);

    /**
     * Get all active API keys available to a user
     * - Admin: sees all active API keys
     * - Teacher: sees active global keys and their own active keys
     *
     * @param principal The authenticated user
     * @return List of available API key DTOs
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    List<AvailableKeys> getAvailableApiKeys(Principal principal);

    /**
     * Assign an API key to a test for a specific group
     * - Admin: must specify a group ID
     * - Teacher: can assign to a specific group or to all their groups if groupId not specified
     * - Teachers can only assign global keys or their own keys
     *
     * @param request The assignment request containing test ID, API key ID, and optional group ID
     * @param principal The authenticated user
     * @throws ResourceNotFoundException if the test, API key, or group doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to access the API key or group
     * @throws ValidationException if an admin doesn't specify a group ID
     * @throws StateConflictException if the group is not associated with the test
     */
    void assignApiKeyToTestForGroup(TestApiKeyAssignmentRequest request, Principal principal);

    /**
     * Unassign API key from a test for a specific group
     * - Admin: must specify a group ID
     * - Teacher: can unassign from a specific group or from all their groups if groupId not specified
     *
     * @param testId The ID of the test
     * @param groupId The ID of the group (optional for teachers, required for admins)
     * @param principal The authenticated user
     * @throws ResourceNotFoundException if the test, group, or assignment doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to access the group
     * @throws StateConflictException if the test doesn't have an API key assigned for the group
     * @throws ValidationException if an admin doesn't specify a group ID
     */
    void unassignApiKeyFromTest(Long testId, Long groupId, Principal principal);

    /**
     * Retrieves a list of all API keys assigned to a test, including group and teacher information
     * - Admin: can see all API key assignments for the test
     * - Teacher: can only see API key assignments for groups where they are the teacher
     *
     * @param testId The ID of the test
     * @param principal The authenticated user
     * @return DTO containing list of API key assignments with group and teacher details
     * @throws ResourceNotFoundException if the test doesn't exist
     * @throws AccessDeniedException if a teacher has no groups associated with the test
     */
    TestApiKeysDTO getTestApiKeys(Long testId, Principal principal);
}
