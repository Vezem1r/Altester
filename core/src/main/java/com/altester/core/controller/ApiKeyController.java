package com.altester.core.controller;

import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.AvailableKeys;
import com.altester.core.service.ApiKeyService;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
