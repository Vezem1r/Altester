package com.altester.core.controller;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class TestController {
    private final TestService testService;

    @GetMapping("/admin/tests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TestSummaryDTO>> getAllTestsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean isActive) {
        log.info("Fetching admin tests with searchQuery: '{}', isActive: {}", searchQuery, isActive);
        Page<TestSummaryDTO> tests = testService.getAllTestsForAdmin(PageRequest.of(page, size), principal, searchQuery, isActive);
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/teacher/tests/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Page<TestSummaryDTO>> getTeacherTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean isActive,
    @RequestParam(required = false) Boolean allowTeacherEdit) {
        Page<TestSummaryDTO> tests = testService.getTeacherTests(PageRequest.of(page, size),
                principal, searchQuery, isActive, allowTeacherEdit);
        return ResponseEntity.ok(tests);
    }

    @PostMapping("/teacher/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> createTest(@Valid @RequestBody CreateTestDTO createTestDTO, Principal principal) {
        TestPreviewDTO createdTest = testService.createTest(createTestDTO, principal);
        return ResponseEntity.ok(createdTest);
    }

    @PutMapping("/teacher/tests/{testId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> updateTest(
            @PathVariable Long testId,
            @Valid @RequestBody CreateTestDTO updateTestDTO,
            Principal principal) {
        TestPreviewDTO updatedTest = testService.updateTest(updateTestDTO, testId, principal);
        return ResponseEntity.ok(updatedTest);
    }

    @DeleteMapping("/teacher/tests/{testId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> deleteTest(@PathVariable Long testId, Principal principal) {
        testService.deleteTest(testId, principal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teacher/tests/{testId}/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestSummaryDTO> getTestSummary(@PathVariable Long testId, Principal principal) {
        TestSummaryDTO test = testService.getTestSummary(testId, principal);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/teacher/tests/{testId}/preview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> getTestPreview(@PathVariable Long testId, Principal principal) {
        TestPreviewDTO test = testService.getTestPreview(testId, principal);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/teacher/tests/subject/{subjectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<TestSummaryDTO>> getTestsBySubject(
            @PathVariable Long subjectId,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean isActive) {
        log.info("Fetching tests for subject {} with searchQuery: '{}', isActive: {}", subjectId, searchQuery, isActive);
        Page<TestSummaryDTO> tests = testService.getTestsBySubject(subjectId, principal, searchQuery, isActive, PageRequest.of(page, size));
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/teacher/tests/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<TestSummaryDTO>> getTestsByGroup(
            @PathVariable Long groupId,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean isActive) {
        log.info("Fetching tests for group {} with searchQuery: '{}', isActive: {}", groupId, searchQuery, isActive);
        Page<TestSummaryDTO> tests = testService.getTestsByGroup(groupId, principal, searchQuery, isActive, PageRequest.of(page, size));
        return ResponseEntity.ok(tests);
    }

    @PutMapping("/teacher/tests/{testId}/activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> toggleTestActivity(@PathVariable Long testId, Principal principal) {
        testService.toggleTestActivity(testId, principal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tests/evaluation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> toggleTestEvaluation(@RequestParam Long testId, @RequestParam Long groupId, Principal principal) {
        testService.toggleAiEvaluation(testId, groupId, principal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/tests/{testId}/teacher-edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleTeacherEditPermission(@PathVariable Long testId, Principal principal) {
        testService.toggleTeacherEditPermission(testId, principal);
        return ResponseEntity.ok().build();
    }
}