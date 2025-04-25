package com.altester.core.controller;

import com.altester.core.service.ApiKeyService;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/getAll")
    public CacheablePage<?> getAllApiKeys(Principal principal) {
        CacheablePage<?> resultPage = apiKeyService.getAll(principal);
        return ResponseEntity.ok(resultPage);
    }
}
