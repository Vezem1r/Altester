package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
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

    public Page<SubjectDTO> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable).map(subject -> {

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
        });
    }

    public void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId) {
        try {
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject not found");
            });

            Optional<Subject> optionalSubject = subjectRepository.findByShortName(createSubjectDTO.getShortName().toUpperCase());
            if (optionalSubject.isPresent() && optionalSubject.get().getId() != subject.getId()) {
                log.error("Subject with short name: {} already exists", createSubjectDTO.getShortName());
                throw new RuntimeException("Subject with short name: " + createSubjectDTO.getShortName() + " already exists");
            }

            subject.setName(createSubjectDTO.getName());
            subject.setShortName(createSubjectDTO.getShortName().toUpperCase());
            subject.setDescription(createSubjectDTO.getDescription());
            subject.setModified(LocalDateTime.now());

            subjectRepository.save(subject);

        } catch (Exception e) {
            log.error("Error during subject update: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteSubject(long subjectId) {
        try {
            subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject with id " + subjectId + " not found");
            });
            subjectRepository.deleteById(subjectId);
        } catch (Exception e) {
            log.error("Error during subject delete: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createSubject(CreateSubjectDTO createSubjectDTO) {
        try {
            Optional<Subject> optSubject = subjectRepository.findByShortName(createSubjectDTO.getShortName().toUpperCase());
            if (optSubject.isPresent()) {
                log.error("Subject with short name {} already exists", createSubjectDTO.getShortName());
                throw new RuntimeException("Subject with short name already exists");
            }

            Subject subject = Subject.builder()
                    .name(createSubjectDTO.getName())
                    .shortName(createSubjectDTO.getShortName().toUpperCase())
                    .description(createSubjectDTO.getDescription())
                    .modified(LocalDateTime.now())
                    .build();

            subjectRepository.save(subject);
            log.info("Subject with short name {} created", createSubjectDTO.getShortName());
        } catch (Exception e) {
            log.error("Error creating subject {}", createSubjectDTO.getShortName());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void updateGroups(UpdateGroupsDTO updateGroupsDTO) {
        try {
            Subject subject = subjectRepository.findById(updateGroupsDTO.getSubjectId())
                    .orElseThrow(() -> {
                        log.error("Subject with id {} not found", updateGroupsDTO.getSubjectId());
                        return new RuntimeException("Subject with id " + updateGroupsDTO.getSubjectId() + " not found");
                    });

            List<Group> validGroupsList = new ArrayList<>();

            for (Long groupId : updateGroupsDTO.getGroupIds()) {
                Group group = groupRepository.findById(groupId).orElse(null);
                if (group == null) {
                    continue;
                }

                groupActivityService.checkAndUpdateGroupActivity(group);

                if (group.isActive() || groupActivityService.isGroupInFuture(group)) {
                    Optional<Subject> existingSubject = subjectRepository.findByGroupsContaining(group);
                    if (existingSubject.isPresent() && existingSubject.get().getId() != subject.getId()) {
                        throw new RuntimeException("Group " + group.getName() + " is already assigned to another subject.");
                    }
                    validGroupsList.add(group);
                } else {
                    log.info("Skipping inactive past group: {}", group.getName());
                }
            }

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

        } catch (Exception e) {
            log.error("Error updating groups in subject {}. {}", updateGroupsDTO.getSubjectId(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void updateGroup(long subjectId, long groupId) {
        try {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> {
                        log.error("Subject with id {} not found", subjectId);
                        return new RuntimeException("Subject with id " + subjectId + " not found");
                    });

            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> {
                        log.error("Group with id {} not found", groupId);
                        return new RuntimeException("Group with id " + groupId + " not found");
                    });

            groupActivityService.checkAndUpdateGroupActivity(group);

            if (!group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                log.info("Skipping inactive past group: {}", group.getName());
                return;
            }

            Optional<Subject> existingSubject = subjectRepository.findByGroupsContaining(group);
            if (existingSubject.isPresent() && existingSubject.get().getId() != subject.getId()) {
                log.error("Group {} is already assigned to another subject", group.getName());
                throw new RuntimeException("Group " + group.getName() + " is already assigned to another subject.");
            }

            if (!subject.getGroups().contains(group)) {
                subject.getGroups().add(group);
                subject.setModified(LocalDateTime.now());
                subjectRepository.save(subject);
                log.info("Group {} added to subject {}", group.getName(), subject.getName());
            } else {
                log.warn("Group {} is already assigned to subject {}", group.getName(), subject.getName());
            }

        } catch (Exception e) {
            log.error("Error updating subject {}: {}", subjectId, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}