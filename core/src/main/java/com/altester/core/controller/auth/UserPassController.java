package com.altester.core.controller.auth;

import com.altester.core.dtos.auth_service.pass.ChangePassDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/password")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserPassController {

    private final RestTemplate restTemplate;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${INTERNAL_API_KEY}")
    private String secretKey;

    private static final String API_KEY_HEADER = "x-api-key";

    private String getAuthServiceUrl() {
        return authServiceUrl + "/password";
    }

    private ResponseEntity<String> forwardRequest(String endpoint, HttpMethod method, HttpEntity<?> requestEntity) {
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
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        log.info("Forwarding password reset request for email: {}", email);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return forwardRequest("/request?email=" + email, HttpMethod.POST, requestEntity);
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody ChangePassDTO changePassDTO) {
        log.info("Forwarding password reset confirmation for email: {}", changePassDTO.getEmail());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<ChangePassDTO> requestEntity = new HttpEntity<>(changePassDTO, headers);
        return forwardRequest("/confirm", HttpMethod.POST, requestEntity);
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendPasswordCode(@RequestParam String email) {
        log.info("Forwarding password reset code resend request for email: {}", email);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return forwardRequest("/resend?email=" + email, HttpMethod.POST, requestEntity);
    }
}
