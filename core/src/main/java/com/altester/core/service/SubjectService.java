package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.util.CacheablePage;
import com.altester.core.exception.*;

public interface SubjectService {

    /**
     * Creates a new subject with the provided information.
     *
     * @param createSubjectDTO DTO containing subject information (name, shortName, description)
     * @throws ResourceAlreadyExistsException if a subject with the same short name already exists
     * @throws IllegalArgumentException if subject data is null
     */
    void createSubject(CreateSubjectDTO createSubjectDTO);

    /**
     * Updates an existing subject with new information.
     *
     * @param createSubjectDTO DTO containing updated subject information
     * @param subjectId ID of the subject to update
     * @throws ResourceNotFoundException if the subject is not found
     * @throws ResourceAlreadyExistsException if the new short name is already used by another subject
     * @throws IllegalArgumentException if subject data is null
     */
    void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId);

    /**
     * Deletes a subject and removes all its associations.
     *
     * @param subjectId ID of the subject to delete
     * @throws ResourceNotFoundException if the subject is not found
     * @throws RuntimeException if there's an error during deletion
     */
    void deleteSubject(long subjectId);

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