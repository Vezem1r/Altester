package com.altester.core.controller.page;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
@Slf4j
@Validated
@PreAuthorize("hasRole('STUDENT')")
public class StudentPageController {

    private final StudentService studentService;

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getStudentDashboard(
            Principal principal,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long groupId) {
        StudentDashboardResponse dashboard = studentService.getStudentDashboard(principal, searchQuery, groupId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/academic-history")
    public ResponseEntity<AcademicHistoryResponse> getAcademicHistory(
            Principal principal,
            @RequestParam(required = true) Integer academicYear,
            @RequestParam(required = true) Semester semester,
            @RequestParam(required = false) String searchQuery) {
        AcademicHistoryResponse history = studentService.getAcademicHistory(principal, academicYear, semester, searchQuery);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/available-periods")
    public ResponseEntity<AvailablePeriodsResponse> getAvailablePeriods(Principal principal) {
        AvailablePeriodsResponse periods = studentService.getAvailablePeriods(principal);
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/test-attempts/{testId}")
    public ResponseEntity<StudentAttemptsResponse> getStudentTestAttempts(Principal principal, @PathVariable Long testId) {
        StudentAttemptsResponse attempts = studentService.getStudentTestAttempts(principal, testId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/attempt-review/{attemptId}")
    public ResponseEntity<AttemptReviewDTO> getAttemptReview(Principal principal, @PathVariable Long attemptId) {
        AttemptReviewDTO review = studentService.getAttemptReview(principal, attemptId);
        return ResponseEntity.ok(review);
    }
}