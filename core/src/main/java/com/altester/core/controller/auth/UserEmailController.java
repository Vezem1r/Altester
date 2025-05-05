package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.email.EmailConfirmDTO;
import com.altester.core.dtos.auth_service.email.EmailInitDTO;
import com.altester.core.dtos.auth_service.email.EmailResendDTO;
import com.altester.core.model.auth.User;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;

@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserEmailController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${INTERNAL_API_KEY}")
    private String secretKey;

    private static final String API_KEY_HEADER = "x-api-key";

    private String getAuthServiceUrl() {
        return authServiceUrl + "/email";
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(principal.getName()));
    }

    private ResponseEntity<?> forwardRequest(String endpoint, HttpMethod method, HttpEntity<?> requestEntity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(API_KEY_HEADER, secretKey);

            if (requestEntity != null) {
                headers.addAll(requestEntity.getHeaders());
                requestEntity = new HttpEntity<>(requestEntity.getBody(), headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            ResponseEntity<String> responseEntity = restTemplate.exchange(getAuthServiceUrl() + endpoint, method, requestEntity, String.class);
            return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Request error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientResponseException e) {
            log.error("RestClientResponseException: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestEmailReset(@RequestBody EmailInitDTO emailInitDTO, Principal principal) {
        String username = principal.getName();
        log.info("Forwarding email reset request for email: {}", emailInitDTO.getEmail());

        emailInitDTO.setUsername(username);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailInitDTO> requestEntity = new HttpEntity<>(emailInitDTO, headers);
        return forwardRequest("/request", HttpMethod.POST, requestEntity);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendEmail(@RequestBody EmailResendDTO emailResendDTO, Principal principal) {
        String username = principal.getName();
        log.info("Forwarding email resend request for email: {}", emailResendDTO.getEmail());

        emailResendDTO.setUsername(username);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailResendDTO> requestEntity = new HttpEntity<>(emailResendDTO, headers);
        return forwardRequest("/resend", HttpMethod.POST, requestEntity);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody EmailConfirmDTO emailConfirmDTO, Principal principal) {
        User user = getUserByPrincipal(principal);
        log.info("Forwarding email confirmation request for email: {}", emailConfirmDTO.getEmail());

        emailConfirmDTO.setUserId(user.getId());
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<EmailConfirmDTO> requestEntity = new HttpEntity<>(emailConfirmDTO, headers);
        return forwardRequest("/confirm", HttpMethod.POST, requestEntity);
    }
}
