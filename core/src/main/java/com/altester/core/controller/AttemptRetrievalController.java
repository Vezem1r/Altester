package com.altester.core.controller;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.dtos.core_service.review.AttemptReviewSubmissionDTO;
import com.altester.core.dtos.core_service.student.AttemptReviewDTO;
import com.altester.core.service.AttemptRetrievalService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
      Principal principal,
      @PathVariable Long testId,
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

  @GetMapping("/teacher/test/{testId}/student/{username}")
  @PreAuthorize("hasRole('TEACHER')")
  public ResponseEntity<List<AttemptInfoDTO>> getStudentTestAttemptsForTeacher(
      Principal principal, @PathVariable Long testId, @PathVariable String username) {
    List<AttemptInfoDTO> attempts =
        attemptRetrievalService.getStudentTestAttemptsForTeacher(principal, testId, username);
    return ResponseEntity.ok(attempts);
  }

  @GetMapping("/admin/test/{testId}/student/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<AttemptInfoDTO>> getStudentTestAttemptsForAdmin(
      Principal principal, @PathVariable Long testId, @PathVariable String username) {
    List<AttemptInfoDTO> attempts =
        attemptRetrievalService.getStudentTestAttemptsForAdmin(principal, testId, username);
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

  @GetMapping("/review/{attemptId}")
  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  public ResponseEntity<AttemptReviewDTO> getAttemptReview(
      Principal principal, @PathVariable Long attemptId) {
    AttemptReviewDTO review = attemptRetrievalService.getAttemptReview(principal, attemptId);
    return ResponseEntity.ok(review);
  }

  @PostMapping("/review/{attemptId}")
  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  public ResponseEntity<Void> submitAttemptReview(
      Principal principal,
      @PathVariable Long attemptId,
      @RequestBody AttemptReviewSubmissionDTO reviewSubmission) {
    attemptRetrievalService.submitAttemptReview(principal, attemptId, reviewSubmission);
    return ResponseEntity.ok().build();
  }
}
