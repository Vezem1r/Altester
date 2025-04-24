package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import com.altester.core.util.CacheablePage;

public interface SubjectService {
    void createSubject(CreateSubjectDTO createSubjectDTO);
    void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId);
    void deleteSubject(long subjectId);
    void updateGroups(UpdateGroupsDTO updateGroupsDTO);
    void updateGroup(long subjectId, long groupId);
    CacheablePage<SubjectDTO> getAllSubjects(int page, int size, String searchQuery);
}