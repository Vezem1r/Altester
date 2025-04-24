package com.altester.core.serviceImpl.test;

import com.altester.core.dtos.core_service.test.CreateTestDTO;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestGroupSelectionService {
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final GroupActivityService groupActivityService;

    /**
     * Identifies and returns valid groups for test assignment based on user role and permissions.
     * Administrators can assign tests to any active group or by subject.
     * Teachers can only assign tests to their own active groups.
     * Inactive groups are silently skipped when using subjectId.
     *
     * @param currentUser The user performing the operation
     * @param createTestDTO The test creation/update data
     * @return List of valid groups for test assignment
     */
    public List<Group> findValidGroupsForTest(User currentUser, CreateTestDTO createTestDTO) {
        List<Group> selectedGroups = new ArrayList<>();
        List<Group> invalidGroups = new ArrayList<>();

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            if (createTestDTO.getSubjectId() != null) {
                Subject subject = subjectRepository.findById(createTestDTO.getSubjectId())
                        .orElseThrow(() -> ResourceNotFoundException.subject(createTestDTO.getSubjectId()));

                if (subject.getGroups() != null && !subject.getGroups().isEmpty()) {
                    for (Group group : subject.getGroups()) {
                        if (groupActivityService.canModifyGroup(group)) {
                            selectedGroups.add(group);
                        } else {
                            log.warn("Skipping inactive group {} from subject {}", group.getName(), subject.getShortName());
                        }
                    }
                }
            } else if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> ResourceNotFoundException.group(groupId));

                    if (groupActivityService.canModifyGroup(group)) {
                        selectedGroups.add(group);
                    } else {
                        invalidGroups.add(group);
                    }
                }

                if (!invalidGroups.isEmpty()) {
                    String invalidGroupNames = invalidGroups.stream()
                            .map(Group::getName)
                            .collect(Collectors.joining(", "));
                    throw StateConflictException.inactiveGroup(invalidGroupNames);
                }
            }
        } else if (currentUser.getRole() == RolesEnum.TEACHER) {
            if (createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty()) {
                List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

                for (Long groupId : createTestDTO.getGroupIds()) {
                    Group group = groupRepository.findById(groupId)
                            .orElseThrow(() -> ResourceNotFoundException.group(groupId));

                    if (teacherGroups.contains(group)) {
                        if (groupActivityService.canModifyGroup(group)) {
                            selectedGroups.add(group);
                        } else {
                            invalidGroups.add(group);
                        }
                    }
                }

                if (!invalidGroups.isEmpty()) {
                    String invalidGroupNames = invalidGroups.stream()
                            .map(Group::getName)
                            .collect(Collectors.joining(", "));
                    throw StateConflictException.inactiveGroup(invalidGroupNames);
                }
            }
        }

        if (selectedGroups.isEmpty()) {
            throw ValidationException.groupValidation("No valid groups selected for test assignment");
        }

        return selectedGroups;
    }
}