package com.altester.auth.models.enums;

import static com.altester.auth.models.enums.Permission.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
@Slf4j
public enum RolesEnum {
  ADMIN(
      Set.of(
          ADMIN_READ,
          ADMIN_CREATE,
          ADMIN_UPDATE,
          ADMIN_DELETE,
          STUDENT_READ,
          STUDENT_CREATE,
          STUDENT_UPDATE,
          STUDENT_DELETE,
          TEACHER_READ,
          TEACHER_CREATE,
          TEACHER_UPDATE,
          TEACHER_DELETE)),
  STUDENT(Set.of(STUDENT_READ, STUDENT_CREATE, STUDENT_UPDATE, STUDENT_DELETE)),
  TEACHER(Set.of(TEACHER_READ, TEACHER_CREATE, TEACHER_UPDATE, TEACHER_DELETE)),
  GUEST(Collections.emptySet());

  private final Set<Permission> permission;

  public List<SimpleGrantedAuthority> getAuthorities() {
    var authorities =
        permission.stream()
            .map(p -> new SimpleGrantedAuthority(p.getPermission()))
            .collect(Collectors.toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    log.info("Authorities: {}", authorities);
    return authorities;
  }
}
