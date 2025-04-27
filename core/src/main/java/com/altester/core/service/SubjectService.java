package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
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
     * Updates the association between a subject and multiple groups.
     * Can associate and disassociate groups with the subject in a single operation.
     *
     * @param updateGroupsDTO DTO containing subject ID and group IDs to associate
     * @throws ResourceNotFoundException if the subject or any group doesn't exist
     */
    void updateGroups(UpdateGroupsDTO updateGroupsDTO);

    /**
     * Updates the association between a subject and a single group.
     * If the group is already associated with another subject, it will be moved.
     *
     * @param subjectId ID of the subject
     * @param groupId ID of the group to associate with the subject
     * @throws ResourceNotFoundException if the subject or group doesn't exist
     */
    void updateGroup(long subjectId, long groupId);

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