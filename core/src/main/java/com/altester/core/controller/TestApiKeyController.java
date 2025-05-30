package com.altester.core.controller;

import com.altester.core.dtos.core_service.apiKey.TestApiKeyAssignmentRequest;
import com.altester.core.dtos.core_service.apiKey.TestApiKeysDTO;
import com.altester.core.service.ApiKeyService;
import jakarta.validation.Valid;
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

  @PostMapping("/assign")
  public ResponseEntity<String> assignApiKeyToTest(
      @Valid @RequestBody TestApiKeyAssignmentRequest request, Principal principal) {
    apiKeyService.assignApiKeyToTestForGroup(request, principal);
    return ResponseEntity.ok("Assigned api key to test");
  }

  @PostMapping("/unassign")
  public ResponseEntity<String> unassignApiKeyFromTest(
      @RequestParam Long testId,
      @RequestParam(required = false) Long groupId,
      Principal principal) {
    apiKeyService.unassignApiKeyFromTest(testId, groupId, principal);
    return ResponseEntity.ok("Unassigned api key from test");
  }

  @GetMapping("/{testId}/api-keys")
  public ResponseEntity<TestApiKeysDTO> getTestApiKeys(
      @PathVariable Long testId, Principal principal) {
    TestApiKeysDTO testApiKeysDTO = apiKeyService.getTestApiKeys(testId, principal);
    return ResponseEntity.ok(testApiKeysDTO);
  }
}
