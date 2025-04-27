package com.altester.core.controller;

import com.altester.core.dtos.core_service.apiKey.TestApiKeyAssignmentRequest;
import com.altester.core.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/tests/apiKeys")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TestApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/assign")
    public ResponseEntity<String> assignApiKeyToTest(
            @Valid @RequestBody TestApiKeyAssignmentRequest request,
            Principal principal) {
        apiKeyService.assignApiKeyToTestForGroup(request, principal);
        return ResponseEntity.ok("Assigned api key to test");
    }
}
