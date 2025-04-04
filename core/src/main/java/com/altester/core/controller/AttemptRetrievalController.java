package com.altester.core.controller;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.service.AttemptRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attempts")
@Slf4j
@Validated
public class AttemptRetrievalController {

    private final AttemptRetrievalService attemptRetrievalService;

    @GetMapping("/teacher/test/{testId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TestAttemptsForGroupDTO>> getTestAttemptsForTeacher(
            Principal principal, @PathVariable Long testId,
            @RequestParam(required = false) String searchQuery) {
        List<TestAttemptsForGroupDTO> attempts =
                attemptRetrievalService.getTestAttemptsForTeacher(principal, testId, searchQuery);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/admin/test/{testId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TestAttemptsForGroupDTO>> getTestAttemptsForAdmin(
            Principal principal,
            @PathVariable Long testId,
            @RequestParam(required = false) String searchQuery) {
        List<TestAttemptsForGroupDTO> attempts =
                attemptRetrievalService.getTestAttemptsForAdmin(principal, testId, searchQuery);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/teacher/student")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentTestAttemptsResponseDTO> getStudentAttemptsForTeacher(
            Principal principal,
            @RequestParam String username,
            @RequestParam(required = false) String searchQuery) {
        StudentTestAttemptsResponseDTO attempts =
                attemptRetrievalService.getStudentAttemptsForTeacher(principal, username, searchQuery);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/admin/student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentTestAttemptsResponseDTO> getStudentAttemptsForAdmin(
            Principal principal,
            @RequestParam String username,
            @RequestParam(required = false) String searchQuery) {
        StudentTestAttemptsResponseDTO attempts =
                attemptRetrievalService.getStudentAttemptsForAdmin(principal, username, searchQuery);
        return ResponseEntity.ok(attempts);
    }
}