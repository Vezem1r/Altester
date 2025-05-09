package com.altester.core.serviceImpl.attemptRetrieval;

import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptAccessValidator {
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public User getUserFromPrincipal(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
  }

  /**
   * Verifies that the user has permission to access the attempt. Admin has unlimited access.
   * Teacher can only access attempts from students in their groups.
   */
  public void verifyAttemptAccessPermission(User user, Attempt attempt) {
    if (user.getRole() == RolesEnum.ADMIN) {
      return;
    }

    if (user.getRole() == RolesEnum.TEACHER) {
      User student = attempt.getStudent();
      boolean hasAccess =
          groupRepository.findByTeacher(user).stream()
              .anyMatch(group -> group.getStudents().contains(student));

      if (!hasAccess) {
        throw AccessDeniedException.attemptAccess();
      }
    } else {
      throw AccessDeniedException.roleConflict();
    }
  }

  public void verifyTeacherRole(User user) {
    if (user.getRole() != RolesEnum.TEACHER) {
      throw AccessDeniedException.roleConflict();
    }
  }

  public void verifyAdminRole(User user) {
    if (user.getRole() != RolesEnum.ADMIN) {
      throw AccessDeniedException.roleConflict();
    }
  }
}
