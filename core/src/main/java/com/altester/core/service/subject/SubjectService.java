package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import com.altester.core.exception.ResourceAlreadyExistsException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;
    private final GroupActivityService groupActivityService;

    /**
     * Retrieves a paginated list of subjects with optional search filtering
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @param searchQuery Optional search term to filter subjects by name or short name (case-insensitive)
     * @return Page of SubjectDTO objects containing subject details and associated groups
     */
    public Page<SubjectDTO> getAllSubjects(Pageable pageable, String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return subjectRepository.findAll(pageable).map(this::convertToSubjectDTO);
        } else {
            return subjectRepository
                    .findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(
                            searchQuery, searchQuery, pageable)
                    .map(this::convertToSubjectDTO);
        }
    }

    /**
     * Converts a Subject entity to SubjectDTO with associated groups information
     *
     * @param subject The Subject entity to convert
     * @return SubjectDTO containing subject details and simplified group information
     */
    private SubjectDTO convertToSubjectDTO(Subject subject) {
        List<SubjectGroupDTO> groups = subject.getGroups().stream()
                .map(group -> new SubjectGroupDTO(group.getId(), group.getName()))
                .toList();

        return new SubjectDTO(
                subject.getId(),
                subject.getName(),
                subject.getShortName(),
                subject.getDescription(),
                subject.getModified(),
                groups
        );
    }

    /**
     * Updates an existing subject with new information
     *
     * @param createSubjectDTO DTO containing updated subject information
     * @param subjectId ID of the subject to update
     * @throws IllegalArgumentException if subject data is null
     * @throws ResourceNotFoundException if subject with given ID doesn't exist
     * @throws ResourceAlreadyExistsException if another subject with the same short name already exists
     */
    @Transactional
    public void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId) {
        if (createSubjectDTO == null) {
            throw new IllegalArgumentException("Subject data cannot be null");
        }

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
            log.error("Subject with id {} not found", subjectId);
            return ResourceNotFoundException.subject(subjectId);
        });

        String shortName = createSubjectDTO.getShortName().toUpperCase();
        Optional<Subject> optionalSubject = subjectRepository.findByShortName(shortName);
        if (optionalSubject.isPresent() && optionalSubject.get().getId() != subject.getId()) {
            log.error("Subject with short name: {} already exists", shortName);
            throw ResourceAlreadyExistsException.subject(shortName);
        }

        subject.setName(createSubjectDTO.getName());
        subject.setShortName(shortName);
        subject.setDescription(createSubjectDTO.getDescription());
        subject.setModified(LocalDateTime.now());

        subjectRepository.save(subject);
        log.info("Subject with id {} updated successfully", subjectId);
    }

    /**
     * Deletes a subject by ID
     *
     * @param subjectId ID of the subject to delete
     * @throws ResourceNotFoundException if subject with given ID doesn't exist
     * @throws RuntimeException if deletion fails
     */
    @Transactional
    public void deleteSubject(long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
            log.error("Subject with id {} not found", subjectId);
            return ResourceNotFoundException.subject(subjectId);
        });

        try {
            subjectRepository.delete(subject);
            log.info("Subject with id {} deleted successfully", subjectId);
        } catch (Exception e) {
            log.error("Error during subject delete: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete subject: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new subject with the provided information
     *
     * @param createSubjectDTO DTO containing new subject information
     * @throws IllegalArgumentException if subject data is null
     * @throws ResourceAlreadyExistsException if a subject with the same short name already exists
     */
    @Transactional
    public void createSubject(CreateSubjectDTO createSubjectDTO) {
        if (createSubjectDTO == null) {
            throw new IllegalArgumentException("Subject data cannot be null");
        }

        String shortName = createSubjectDTO.getShortName().toUpperCase();
        Optional<Subject> optSubject = subjectRepository.findByShortName(shortName);
        if (optSubject.isPresent()) {
            log.error("Subject with short name {} already exists", shortName);
            throw ResourceAlreadyExistsException.subject(shortName);
        }

        Subject subject = Subject.builder()
                .name(createSubjectDTO.getName())
                .shortName(shortName)
                .description(createSubjectDTO.getDescription())
                .modified(LocalDateTime.now())
                .build();

        subjectRepository.save(subject);
        log.info("Subject with short name {} created", shortName);
    }

    /**
     * Updates the groups associated with a subject
     *
     * @param updateGroupsDTO DTO containing subject ID and list of group IDs to associate
     * @throws IllegalArgumentException if update data or group IDs are null
     * @throws ResourceNotFoundException if subject with given ID doesn't exist
     * @throws StateConflictException if any group is already assigned to another subject
     */
    @Transactional
    public void updateGroups(UpdateGroupsDTO updateGroupsDTO) {

        if (updateGroupsDTO == null || updateGroupsDTO.getGroupIds() == null) {
            throw new IllegalArgumentException("Update groups data cannot be null");
        }

        Subject subject = subjectRepository.findById(updateGroupsDTO.getSubjectId())
                .orElseThrow(() -> {
                    log.error("Subject with id {} not found", updateGroupsDTO.getSubjectId());
                    return ResourceNotFoundException.subject(updateGroupsDTO.getSubjectId());
                });

        List<Group> validGroupsList = new ArrayList<>();
        List<Long> notFoundGroups = new ArrayList<>();

        for (Long groupId : updateGroupsDTO.getGroupIds()) {
            Optional<Group> groupOpt = groupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                notFoundGroups.add(groupId);
                continue;
            }

            Group group = groupOpt.get();
            if (isGroupEligibleForAssignment(group)) {
                checkGroupAssignment(group, subject.getId());
                validGroupsList.add(group);
            }
        }

        if (!notFoundGroups.isEmpty()) {
            log.warn("Some groups were not found: {}", notFoundGroups);
        }

        updateSubjectGroups(subject, validGroupsList);
    }

    /**
     * Updates the subject groups by adding valid groups and removing ineligible ones
     *
     * @param subject The subject entity to update
     * @param validGroupsList List of eligible groups to add to the subject
     */
    private void updateSubjectGroups(Subject subject, List<Group> validGroupsList) {
        Set<Group> groupsToAdd = new HashSet<>(validGroupsList);
        Set<Group> currentGroups = subject.getGroups();

        Set<Group> groupsToRemove = currentGroups.stream()
                .filter(group -> {
                    groupActivityService.checkAndUpdateGroupActivity(group);
                    return (group.isActive() || groupActivityService.isGroupInFuture(group)) &&
                            !groupsToAdd.contains(group);
                })
                .collect(Collectors.toSet());

        Set<Group> pastInactiveGroups = currentGroups.stream()
                .filter(group -> !group.isActive() && !groupActivityService.isGroupInFuture(group))
                .collect(Collectors.toSet());

        currentGroups.removeAll(groupsToRemove);
        currentGroups.addAll(groupsToAdd);

        subject.setModified(LocalDateTime.now());
        subject.setGroups(currentGroups);
        subjectRepository.save(subject);

        if (!pastInactiveGroups.isEmpty()) {
            log.info("Subject {} groups updated successfully. Kept {} past inactive groups.",
                    subject.getName(), pastInactiveGroups.size());
        } else {
            log.info("Subject {} groups updated successfully", subject.getName());
        }
    }

    /**
     * Checks if a group is eligible for assignment to a subject
     * Only active groups or future groups are eligible
     *
     * @param group The group to check for eligibility
     * @return true if the group is eligible for assignment, false otherwise
     */
    private boolean isGroupEligibleForAssignment(Group group) {
        groupActivityService.checkAndUpdateGroupActivity(group);

        boolean eligible = group.isActive() || groupActivityService.isGroupInFuture(group);
        if (!eligible) {
            log.info("Skipping inactive past group: {}", group.getName());
        }
        return eligible;
    }

    /**
     * Checks if a group can be assigned to a subject
     * Ensures the group is not already assigned to another subject
     *
     * @param group The group to check for existing assignments
     * @param subjectId The ID of the subject to assign the group to
     * @throws StateConflictException if the group is already assigned to another subject
     */
    private void checkGroupAssignment(Group group, long subjectId) {
        Optional<Subject> existingSubject = subjectRepository.findByGroupsContaining(group);
        if (existingSubject.isPresent() && existingSubject.get().getId() != subjectId) {
            throw StateConflictException.groupAlreadyAssigned(group.getName());
        }
    }

    /**
     * Adds a specific group to a subject
     *
     * @param subjectId ID of the subject to update
     * @param groupId ID of the group to add
     * @throws ResourceNotFoundException if subject or group with given IDs don't exist
     * @throws StateConflictException if the group is already assigned to another subject
     * @throws RuntimeException if update fails
     */
    @Transactional
    public void updateGroup(long subjectId, long groupId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    log.error("Subject with id {} not found", subjectId);
                    return ResourceNotFoundException.subject(subjectId);
                });

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.error("Group with id {} not found", groupId);
                    return ResourceNotFoundException.group(groupId);
                });

        if (!isGroupEligibleForAssignment(group)) {
            return;
        }

        try {
            checkGroupAssignment(group, subject.getId());

            if (!subject.getGroups().contains(group)) {
                subject.getGroups().add(group);
                subject.setModified(LocalDateTime.now());
                subjectRepository.save(subject);
                log.info("Group {} added to subject {}", group.getName(), subject.getName());
            } else {
                log.warn("Group {} is already assigned to subject {}", group.getName(), subject.getName());
            }
        } catch (StateConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating subject {} with group {}: {}", subjectId, groupId, e.getMessage(), e);
            throw new RuntimeException("Failed to update subject with group: " + e.getMessage(), e);
        }
    }
}