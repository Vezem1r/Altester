package com.altester.core.serviceImpl.subject;

import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectGroupService {

    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;
    private final GroupActivityService groupActivityService;
    private final CacheService cacheService;

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

        clearCaches();
    }

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
            log.warn("Group {} is not eligible for assignment (inactive past group)", group.getName());
            return;
        }

        try {
            checkGroupAssignment(group, subject.getId());

            if (!subject.getGroups().contains(group)) {
                subject.getGroups().add(group);
                subject.setModified(LocalDateTime.now());
                subjectRepository.save(subject);

                clearCaches();

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
    public boolean isGroupEligibleForAssignment(Group group) {
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
    public void checkGroupAssignment(Group group, long subjectId) {
        Optional<Subject> existingSubject = subjectRepository.findByGroupsContaining(group);
        if (existingSubject.isPresent() && existingSubject.get().getId() != subjectId) {
            throw StateConflictException.groupAlreadyAssigned(group.getName());
        }
    }

    private void clearCaches() {
        cacheService.clearSubjectRelatedCaches();
        cacheService.clearStudentRelatedCaches();
        cacheService.clearAdminRelatedCaches();
        cacheService.clearTestRelatedCaches();
    }
}
