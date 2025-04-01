package com.altester.core.service;

import com.altester.core.dtos.core_service.student.*;
import com.altester.core.model.subject.enums.Semester;

import java.security.Principal;

public interface StudentService {
    StudentDashboardResponse getStudentDashboard(Principal principal, String searchQuery, Long groupId);
    AcademicHistoryResponse getAcademicHistory(Principal principal, Integer academicYear, Semester semester, String searchQuery);
    AvailablePeriodsResponse getAvailablePeriods(Principal principal);
    StudentAttemptsResponse getStudentTestAttempts(Principal principal, Long testId);
    AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId);
}