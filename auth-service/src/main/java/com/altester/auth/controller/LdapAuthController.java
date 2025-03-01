package com.altester.auth.controller;

import com.altester.auth.dto.LdapLoginRequest;
import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.models.User;
import com.altester.auth.service.LdapAuthService;
import com.altester.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ldap")
@RequiredArgsConstructor
@Slf4j
public class LdapAuthController {

    private final LdapAuthService ldapAuthService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LdapLoginRequest request) {
        log.info("Received LDAP login request for user: {}", request.getLogin());

        User authenticatedUser = ldapAuthService.authenticate(request.getLogin(), request.getPassword());

        if (authenticatedUser != null) {
            log.info("User {} authenticated successfully.", request.getLogin());

            String token = jwtService.generateToken(authenticatedUser, authenticatedUser.getRole().name(), false);
            long expirationTimeMillis = jwtService.extractExpiration(token).getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long expiresIn = expirationTimeMillis - currentTimeMillis;

            LoginResponse loginResponse = new LoginResponse(token, expiresIn, "Login successful");
            return ResponseEntity.ok(loginResponse);
        } else {
            log.warn("Authentication failed for user: {}", request.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error during authentication");
        }
    }
}
