package com.altester.core.service;

import com.altester.core.dtos.core_service.TeacherPage.TeacherGroupDetailDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherStudentsDTO;
import com.altester.core.dtos.core_service.subject.GroupsResponse;
import com.altester.core.exception.*;
import com.altester.core.util.CacheablePage;
import java.security.Principal;

public interface TeacherPageService {

  /**
   * Retrieves the teacher dashboard data including all subjects and groups the teacher is assigned
   * to.
   *
   * @param principal The authenticated teacher
   * @return TeacherPageDTO containing the teacher's data, including subjects and groups
   * @throws ResourceNotFoundException if the teacher is not found
   */
  TeacherPageDTO getPage(Principal principal);

  /**
   * Retrieves a paginated and searchable list of all unique students enrolled in the teacher's
   * active groups.
   *
   * @param principal The authenticated teacher
   * @param page Page number (zero-based)
   * @param size Number of items per page
   * @param searchQuery Optional search text to filter students by name, surname, username, or email
   * @return Paginated list of students with associated group information
   * @throws ResourceNotFoundException if the teacher is not found
   */
  CacheablePage<TeacherStudentsDTO> getStudents(
      Principal principal, int page, int size, String searchQuery);

  /**
   * Retrieves a paginated and searchable list of all groups assigned to the teacher, with optional
   * filtering by status (active, inactive, future).
   *
   * @param principal The authenticated teacher
   * @param page Page number (zero-based)
   * @param size Number of items per page
   * @param searchQuery Optional search text to filter groups by name
   * @param statusFilter Optional filter by group status ("active", "inactive", "future", or null
   *     for all)
   * @return Paginated list of groups with relevant information
   * @throws ResourceNotFoundException if the teacher is not found
   */
  CacheablePage<GroupsResponse> getGroups(
      Principal principal, int page, int size, String searchQuery, String statusFilter);

  TeacherGroupDetailDTO getTeacherGroup(Principal principal, Long groupId);
}
