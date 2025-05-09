package com.altester.core.controller;

import com.altester.core.dtos.ai_service.AssignmentPromptRequest;
import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.ApiKeyRequest;
import com.altester.core.dtos.core_service.apiKey.AvailableKeys;
import com.altester.core.service.ApiKeyService;
import com.altester.core.util.CacheablePage;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class ApiKeyController {

  private final ApiKeyService apiKeyService;

  @GetMapping
  public ResponseEntity<CacheablePage<ApiKeyDTO>> getAllApiKeys(Principal principal) {
    CacheablePage<ApiKeyDTO> resultPage = apiKeyService.getAll(principal);
    return ResponseEntity.ok(resultPage);
  }

  @GetMapping("/available")
  public ResponseEntity<List<AvailableKeys>> getAvailableApiKeys(Principal principal) {
    List<AvailableKeys> keys = apiKeyService.getAvailableApiKeys(principal);
    return ResponseEntity.ok(keys);
  }

  @PostMapping
  public ResponseEntity<String> createApiKey(
      Principal principal, @RequestBody @Valid ApiKeyRequest request) {
    apiKeyService.createApiKey(request, principal);
    return ResponseEntity.status(HttpStatus.CREATED).body("API key created successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<String> updateApiKey(
      Principal principal, @PathVariable Long id, @Valid @RequestBody ApiKeyRequest request) {
    apiKeyService.updateApiKey(id, request, principal);
    return ResponseEntity.ok("API key updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteApiKey(@PathVariable Long id, Principal principal) {
    boolean deleted = apiKeyService.deleteApiKey(id, principal);
    return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/toggle")
  public ResponseEntity<Map<String, Boolean>> toggleApiKey(
      @PathVariable Long id, Principal principal) {
    boolean isActive = apiKeyService.toggleApiKeyStatus(id, principal);
    return ResponseEntity.ok(Map.of("active", isActive));
  }

  @PutMapping("/assignment-prompt")
  public ResponseEntity<Void> updateAssignmentPrompt(
      @RequestBody AssignmentPromptRequest request, Principal principal) {
    apiKeyService.updateAssignmentPrompt(request, principal);
    return ResponseEntity.ok().build();
  }
}
