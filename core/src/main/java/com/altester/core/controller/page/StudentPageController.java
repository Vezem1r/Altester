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
        log.info("Getting dashboard for student with searchQuery: {}, groupId: {}", searchQuery, groupId);
        StudentDashboardResponse dashboard = studentService.getStudentDashboard(principal, searchQuery, groupId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/academic-history")
    public ResponseEntity<AcademicHistoryResponse> getAcademicHistory(
            Principal principal,
            @RequestParam(required = true) Integer academicYear,
            @RequestParam(required = true) Semester semester,
            @RequestParam(required = false) String searchQuery) {
        log.info("Getting academic history for student with academicYear: {}, semester: {}, searchQuery: {}",
                academicYear, semester, searchQuery);
        AcademicHistoryResponse history = studentService.getAcademicHistory(principal, academicYear, semester, searchQuery);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/available-periods")
    public ResponseEntity<AvailablePeriodsResponse> getAvailablePeriods(Principal principal) {
        log.info("Getting available academic periods for student");
        AvailablePeriodsResponse periods = studentService.getAvailablePeriods(principal);
        return ResponseEntity.ok(periods);
    }
}