package com.altester.core.controller;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.service.TestService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    Page<TestSummaryDTO> tests =
        testService.getAllTestsForAdmin(
            PageRequest.of(page, size), principal, searchQuery, isActive);
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
    Page<TestSummaryDTO> tests =
        testService.getTeacherTests(
            PageRequest.of(page, size), principal, searchQuery, isActive, allowTeacherEdit);
    return ResponseEntity.ok(tests);
  }

  @GetMapping("/teacher/tests/{testId}/summary")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<TestSummaryDTO> getTestSummary(
      @PathVariable Long testId, Principal principal) {
    TestSummaryDTO test = testService.getTestSummary(testId, principal);
    return ResponseEntity.ok(test);
  }

  @GetMapping("/teacher/tests/{testId}/preview")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<TestPreviewDTO> getTestPreview(
      @PathVariable Long testId, Principal principal) {
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
    log.info(
        "Fetching tests for subject {} with searchQuery: '{}', isActive: {}",
        subjectId,
        searchQuery,
        isActive);
    Page<TestSummaryDTO> tests =
        testService.getTestsBySubject(
            subjectId, principal, searchQuery, isActive, PageRequest.of(page, size));
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
    log.info(
        "Fetching tests for group {} with searchQuery: '{}', isActive: {}",
        groupId,
        searchQuery,
        isActive);
    Page<TestSummaryDTO> tests =
        testService.getTestsByGroup(
            groupId, principal, searchQuery, isActive, PageRequest.of(page, size));
    return ResponseEntity.ok(tests);
  }

  @GetMapping("/teacher/tests/{testId}/questions")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<Page<QuestionDTO>> getTestQuestions(
      @PathVariable Long testId,
      Principal principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) QuestionDifficulty difficulty) {
    Page<QuestionDTO> questions =
        testService.getTestQuestions(testId, principal, PageRequest.of(page, size), difficulty);
    return ResponseEntity.ok(questions);
  }

  @GetMapping("/teacher/tests/{testId}/student-preview")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<Page<QuestionDTO>> getStudentTestPreview(
      @PathVariable Long testId,
      Principal principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<QuestionDTO> question =
        testService.getStudentTestPreview(testId, principal, PageRequest.of(page, size));
    return ResponseEntity.ok(question);
  }
}
