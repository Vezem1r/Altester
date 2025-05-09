package com.altester.core.serviceImpl.apiKey;

import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiKeyAccessValidator {

  private final UserRepository userRepository;

  /**
   * Retrieves the user from the provided principal.
   *
   * @param principal the security principal
   * @return the user entity
   * @throws ResourceNotFoundException if the user is not found
   */
  public User getUserFromPrincipal(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
  }

  /**
   * Validates that the current user has permission to perform the specified operation on the API
   * key.
   *
   * @param currentUser the current user
   * @param apiKey the API key to validate access for
   * @param operation the operation being performed (delete, update, toggle)
   * @throws AccessDeniedException if the user doesn't have permission
   */
  public void validateApiKeyAccessPermission(User currentUser, ApiKey apiKey, String operation) {
    if (!RolesEnum.ADMIN.equals(currentUser.getRole())
        && (apiKey.isGlobal()
            || apiKey.getOwner() == null
            || !apiKey.getOwner().getId().equals(currentUser.getId()))) {
      throw AccessDeniedException.apiKeyAccess("You cannot " + operation + " this API key");
    }
  }

  /**
   * Validates that the group is associated with the test and that the user has access to the group.
   *
   * @param group the group to validate
   * @param user the current user
   * @param test the test to check association with
   * @throws StateConflictException if the group is not associated with the test
   * @throws AccessDeniedException if the user doesn't have permission to access the group
   */
  public void validateGroupAccess(Group group, User user, Test test) {
    if (!group.getTests().contains(test)) {
      throw new StateConflictException(
          "group", "mismatch", "The specified group is not associated with this test");
    }

    if (!RolesEnum.ADMIN.equals(user.getRole())
        && (group.getTeacher() == null || !group.getTeacher().getId().equals(user.getId()))) {
      throw AccessDeniedException.groupAccess();
    }
  }

  /**
   * Validates that the user has permission to create a global API key.
   *
   * @param currentUser the current user
   * @param isGlobalRequest whether the request is for a global API key
   * @throws AccessDeniedException if a non-admin user attempts to create a global API key
   */
  public void validateGlobalApiKeyRequest(User currentUser, boolean isGlobalRequest) {
    if (isGlobalRequest && !RolesEnum.ADMIN.equals(currentUser.getRole())) {
      throw AccessDeniedException.notAdmin();
    }
  }

  /**
   * Validates that admin users specify a group ID when required.
   *
   * @param currentUser the current user
   * @param groupId the group ID from the request
   * @throws ValidationException if an admin doesn't provide a group ID
   */
  public void validateAdminGroupIdRequirement(User currentUser, Long groupId) {
    if (RolesEnum.ADMIN.equals(currentUser.getRole()) && groupId == null) {
      throw ValidationException.invalidParameter("groupId", "Admins must specify a group ID");
    }
  }

  /**
   * Validates that the user has permission to use the API key.
   *
   * @param currentUser the current user
   * @param apiKey the API key to validate usage for
   * @throws AccessDeniedException if the user doesn't have permission to use the API key
   */
  public void validateApiKeyUsagePermission(User currentUser, ApiKey apiKey) {
    if (!RolesEnum.ADMIN.equals(currentUser.getRole())
        && !apiKey.isGlobal()
        && (apiKey.getOwner() == null || !apiKey.getOwner().getId().equals(currentUser.getId()))) {
      throw AccessDeniedException.apiKeyAccess("You don't have access to this API key");
    }
  }
}
