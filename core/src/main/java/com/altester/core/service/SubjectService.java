package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import org.springframework.data.domain.Page;

public interface SubjectService {

    void createSubject(CreateSubjectDTO createSubjectDTO);

    void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId);

    void deleteSubject(long subjectId);

    /**
     * Updates the groups associated with a subject
     * @param updateGroupsDTO DTO containing subject ID and list of group IDs to associate
     */
    void updateGroups(UpdateGroupsDTO updateGroupsDTO);

    /**
     * Adds a specific group to a subject
     */
    void updateGroup(long subjectId, long groupId);

    /**
     * Retrieves a paginated list of subjects with optional search filtering
     * @param searchQuery Optional search term to filter subjects by name or short name
     * @return Page of SubjectDTO objects containing subject details and associated groups
     */
    Page<SubjectDTO> getAllSubjects(int page, int size, String searchQuery);
}