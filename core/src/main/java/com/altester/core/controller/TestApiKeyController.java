package com.altester.core.controller;

import com.altester.core.dtos.core_service.apiKey.TestApiKeysDTO;
import com.altester.core.service.ApiKeyService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tests/apiKeys")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TestApiKeyController {

  private final ApiKeyService apiKeyService;

  @GetMapping("/{testId}/api-keys")
  public ResponseEntity<TestApiKeysDTO> getTestApiKeys(
      @PathVariable Long testId, Principal principal) {
    TestApiKeysDTO testApiKeysDTO = apiKeyService.getTestApiKeys(testId, principal);
    return ResponseEntity.ok(testApiKeysDTO);
  }
}
