package com.altester.core.serviceImpl.apiKey;

import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
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
}
