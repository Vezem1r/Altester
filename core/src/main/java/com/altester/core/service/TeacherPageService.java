package com.altester.core.service;

import com.altester.core.dtos.core_service.TeacherPage.ListTeacherGroupDTO;
import com.altester.core.dtos.core_service.TeacherPage.MoveStudentRequest;
import com.altester.core.dtos.core_service.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherStudentsDTO;
import org.springframework.data.domain.Page;

import java.security.Principal;

public interface TeacherPageService {

    TeacherPageDTO getPage(Principal principal);

    Page<TeacherStudentsDTO> getStudents(Principal principal, int page, int size, String searchQuery);

    Page<ListTeacherGroupDTO> getGroups(Principal principal, int page, int size, String searchQuery, String statusFilter);

    /**
     * Moves a student from one group to another within the same subject.
     */
    void moveStudentBetweenGroups(Principal principal, MoveStudentRequest request);
}