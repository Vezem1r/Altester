package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.exception.*;
import com.altester.core.util.CacheablePage;

public interface SubjectService {

  /**
   * Retrieves a paginated and searchable list of all subjects.
   *
   * @param page Page number (zero-based)
   * @param size Number of items per page
   * @param searchQuery Optional search text to filter subjects by name or short name
   * @return Paginated list of subjects
   */
  CacheablePage<SubjectDTO> getAllSubjects(int page, int size, String searchQuery);
}
