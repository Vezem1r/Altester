package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.pass.ChangePassDTO;
import com.altester.core.model.auth.User;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;

@RequestMapping("/password")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserPassController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    private String getAuthServiceUrl() {
        return authServiceUrl + "/password";
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(principal.getName()));
    }

    private ResponseEntity<String> forwardRequest(String endpoint, HttpMethod method, HttpEntity<?> requestEntity) {
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(getAuthServiceUrl() + endpoint, method, requestEntity, String.class);
            return ResponseEntity.ok(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Request error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request failed: " + e.getMessage());
        }
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(Principal principal) {
        User user = getUserByPrincipal(principal);
        log.info("Forwarding password reset request for email: {}", user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return forwardRequest("/request?email=" + user.getEmail(), HttpMethod.POST, requestEntity);
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody ChangePassDTO changePassDTO, Principal principal) {
        User user = getUserByPrincipal(principal);
        log.info("Forwarding password reset confirmation for email: {}", user.getEmail());

        changePassDTO.setUserId(user.getId());
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<ChangePassDTO> requestEntity = new HttpEntity<>(changePassDTO, headers);
        return forwardRequest("/confirm", HttpMethod.POST, requestEntity);
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendPasswordCode(Principal principal) {
        User user = getUserByPrincipal(principal);
        log.info("Forwarding password reset code resend request for email: {}", user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return forwardRequest("/resend?email=" + user.getEmail(), HttpMethod.POST, requestEntity);
    }
}
