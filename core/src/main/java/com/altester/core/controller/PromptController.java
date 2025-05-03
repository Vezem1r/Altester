package com.altester.core.controller;

import com.altester.core.dtos.ai_service.PromptDTO;
import com.altester.core.dtos.ai_service.PromptDetailsDTO;
import com.altester.core.dtos.ai_service.PromptRequest;
import com.altester.core.service.PromptService;
import com.altester.core.util.CacheablePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/prompts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
@Validated
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    public ResponseEntity<PromptDetailsDTO> createPrompt(
            @Valid @RequestBody PromptRequest request,
            Principal principal) {
        PromptDetailsDTO prompt = promptService.createPrompt(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(prompt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptDetailsDTO> updatePrompt(
            @PathVariable Long id,
            @Valid @RequestBody PromptRequest request,
            Principal principal) {
        PromptDetailsDTO prompt = promptService.updatePrompt(id, request, principal);
        return ResponseEntity.ok(prompt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable Long id,
            Principal principal) {
        promptService.deletePrompt(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDTO> getPromptDetails(
            @PathVariable Long id,
            Principal principal) {
        PromptDetailsDTO prompt = promptService.getPromptDetails(id, principal);
        return ResponseEntity.ok(prompt);
    }

    @GetMapping
    public ResponseEntity<CacheablePage<PromptDTO>> getAllPrompts(
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal) {
        CacheablePage<PromptDTO> prompts = promptService.getAllPrompts(pageable, principal);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/my")
    public ResponseEntity<CacheablePage<PromptDTO>> getMyPrompts(
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal) {
        CacheablePage<PromptDTO> prompts = promptService.getMyPrompts(pageable, principal);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/public")
    public ResponseEntity<CacheablePage<PromptDTO>> getPublicPrompts(
            @PageableDefault(size = 20) Pageable pageable) {
        CacheablePage<PromptDTO> prompts = promptService.getPublicPrompts(pageable);
        return ResponseEntity.ok(prompts);
    }
}
