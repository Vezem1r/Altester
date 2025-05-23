package com.altester.notification.security;

import com.altester.notification.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Set<String> allowedUsernames = Set.of("admin", "teacher", "student", "ADMIN", "TEACHER", "STUDENT");

    if (!allowedUsernames.contains(username)) {
      throw new UsernameNotFoundException(
          "Invalid username. Only admin, teacher, or student are allowed");
    }

    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("User not found with username: " + username));
  }
}
