package com.altester.core.controller;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.service.test.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final TestService testService;

    @GetMapping("/admin/tests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TestSummaryDTO>> getAllTestsForAdmin(Pageable pageable, Principal principal) {
        try {
            Page<TestSummaryDTO> tests = testService.getAllTestsForAdmin(pageable, principal);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            log.error("Error retrieving all tests for admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/tests/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Page<TestSummaryDTO>> getTeacherTests(Pageable pageable, Principal principal) {
        try {
            Page<TestSummaryDTO> tests = testService.getTeacherTests(pageable, principal);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            log.error("Error retrieving teacher tests: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/teacher/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> createTest(@RequestBody CreateTestDTO createTestDTO, Principal principal) {
        try {
            TestPreviewDTO createdTest = testService.createTest(createTestDTO, principal);
            return ResponseEntity.ok(createdTest);
        } catch (Exception e) {
            log.error("Error creating test: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/teacher/tests/{testId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> updateTest(
            @PathVariable Long testId,
            @RequestBody CreateTestDTO updateTestDTO,
            Principal principal) {
        try {
            TestPreviewDTO updatedTest = testService.updateTest(updateTestDTO, testId, principal);
            return ResponseEntity.ok(updatedTest);
        } catch (Exception e) {
            log.error("Error updating test: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/teacher/tests/{testId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> deleteTest(@PathVariable Long testId, Principal principal) {
        try {
            testService.deleteTest(testId, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting test: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/tests/{testId}/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestSummaryDTO> getTestSummary(@PathVariable Long testId, Principal principal) {
        try {
            TestSummaryDTO test = testService.getTestSummary(testId, principal);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            log.error("Error retrieving test summary: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/tests/{testId}/preview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TestPreviewDTO> getTestPreview(@PathVariable Long testId, Principal principal) {
        try {
            TestPreviewDTO test = testService.getTestPreview(testId, principal);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            log.error("Error retrieving test preview: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/tests/subject/{subjectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<TestSummaryDTO>> getTestsBySubject(@PathVariable Long subjectId, Principal principal) {
        try {
            List<TestSummaryDTO> tests = testService.getTestsBySubject(subjectId, principal);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            log.error("Error retrieving tests by subject: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/tests/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<TestSummaryDTO>> getTestsByGroup(@PathVariable Long groupId, Principal principal) {
        try {
            List<TestSummaryDTO> tests = testService.getTestsByGroup(groupId, principal);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            log.error("Error retrieving tests by group: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/teacher/tests/{testId}/activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> toggleTestActivity(@PathVariable Long testId, Principal principal) {
        try {
            testService.toggleTestActivity(testId, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error toggling test activity: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/admin/tests/{testId}/teacher-edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleTeacherEditPermission(@PathVariable Long testId, Principal principal) {
        try {
            testService.toggleTeacherEditPermission(testId, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error toggling teacher edit permission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}