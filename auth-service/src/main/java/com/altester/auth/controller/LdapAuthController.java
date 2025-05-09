package com.altester.auth.controller;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.LdapLoginRequest;
import com.altester.auth.service.LdapAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ldap")
@RequiredArgsConstructor
@Slf4j
public class LdapAuthController {

  private final LdapAuthService ldapAuthService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LdapLoginRequest request) {
    log.info("Received LDAP login request for user: {}", request.getLogin());
    LoginResponse response = ldapAuthService.login(request);
    return ResponseEntity.ok(response);
  }
}
