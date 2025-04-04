package com.altester.core.service;

import com.altester.core.dtos.core_service.retrieval.*;
import java.security.Principal;
import java.util.List;

public interface AttemptRetrievalService {

    List<TestAttemptsForGroupDTO> getTestAttemptsForTeacher(
            Principal principal, Long testId, String searchQuery);

    List<TestAttemptsForGroupDTO> getTestAttemptsForAdmin(
            Principal principal, Long testId, String searchQuery);

    StudentTestAttemptsResponseDTO getStudentAttemptsForTeacher(
            Principal principal, String username, String searchQuery);

    StudentTestAttemptsResponseDTO getStudentAttemptsForAdmin(
            Principal principal, String username, String searchQuery);
}