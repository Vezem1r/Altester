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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestGroupSelectionService {
  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final GroupActivityService groupActivityService;

  /**
   * Identifies and returns valid groups for test assignment based on user role and permissions.
   * Administrators can assign tests to any active group or by subject. Teachers can only assign
   * tests to their own active groups. Inactive groups are silently skipped when using subjectId.
   *
   * @param currentUser The user performing the operation
   * @param createTestDTO The test creation/update data
   * @return List of valid groups for test assignment
   */
  public List<Group> findValidGroupsForTest(User currentUser, CreateTestDTO createTestDTO) {
    if (currentUser.getRole() == RolesEnum.ADMIN) {
      return findGroupsForAdmin(createTestDTO);
    } else if (currentUser.getRole() == RolesEnum.TEACHER) {
      return findGroupsForTeacher(currentUser, createTestDTO);
    }

    return new ArrayList<>();
  }

  private List<Group> findGroupsForAdmin(CreateTestDTO createTestDTO) {
    List<Group> selectedGroups = new ArrayList<>();

    if (createTestDTO.getSubjectId() != null) {
      selectedGroups = findGroupsBySubject(createTestDTO.getSubjectId());
    } else if (hasGroupIds(createTestDTO)) {
      selectedGroups = findAndValidateGroupsByIds(createTestDTO.getGroupIds());
    }

    validateGroupSelection(selectedGroups);
    return selectedGroups;
  }

  private List<Group> findGroupsForTeacher(User teacher, CreateTestDTO createTestDTO) {
    List<Group> selectedGroups = new ArrayList<>();

    if (hasGroupIds(createTestDTO)) {
      selectedGroups = findTeacherGroupsByIds(teacher, createTestDTO.getGroupIds());
    }

    validateGroupSelection(selectedGroups);
    return selectedGroups;
  }

  private boolean hasGroupIds(CreateTestDTO createTestDTO) {
    return createTestDTO.getGroupIds() != null && !createTestDTO.getGroupIds().isEmpty();
  }

  private List<Group> findGroupsBySubject(Long subjectId) {
    Subject subject =
        subjectRepository
            .findById(subjectId)
            .orElseThrow(() -> ResourceNotFoundException.subject(subjectId));

    return getActiveGroupsFromSubject(subject);
  }

  private List<Group> getActiveGroupsFromSubject(Subject subject) {
    List<Group> selectedGroups = new ArrayList<>();

    if (subject.getGroups() != null && !subject.getGroups().isEmpty()) {
      for (Group group : subject.getGroups()) {
        if (groupActivityService.canModifyGroup(group)) {
          selectedGroups.add(group);
        } else {
          log.warn(
              "Skipping inactive group {} from subject {}",
              group.getName(),
              subject.getShortName());
        }
      }
    }

    return selectedGroups;
  }

  private List<Group> findAndValidateGroupsByIds(Set<Long> groupIds) {
    List<Group> selectedGroups = new ArrayList<>();
    List<Group> invalidGroups = new ArrayList<>();

    for (Long groupId : groupIds) {
      Group group = findGroupById(groupId);
      addToAppropriateList(group, selectedGroups, invalidGroups);
    }

    throwIfInvalidGroups(invalidGroups);
    return selectedGroups;
  }

  private Group findGroupById(Long groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> ResourceNotFoundException.group(groupId));
  }

  private void addToAppropriateList(Group group, List<Group> valid, List<Group> invalid) {
    if (groupActivityService.canModifyGroup(group)) {
      valid.add(group);
    } else {
      invalid.add(group);
    }
  }

  private List<Group> findTeacherGroupsByIds(User teacher, Set<Long> groupIds) {
    List<Group> teacherGroups = groupRepository.findByTeacher(teacher);
    List<Group> selectedGroups = new ArrayList<>();
    List<Group> invalidGroups = new ArrayList<>();

    for (Long groupId : groupIds) {
      Group group = findGroupById(groupId);
      processTeacherGroup(group, teacherGroups, selectedGroups, invalidGroups);
    }

    throwIfInvalidGroups(invalidGroups);
    return selectedGroups;
  }

  private void processTeacherGroup(
      Group group, List<Group> teacherGroups, List<Group> validGroups, List<Group> invalidGroups) {
    if (teacherGroups.contains(group)) {
      addToAppropriateList(group, validGroups, invalidGroups);
    }
  }

  private void throwIfInvalidGroups(List<Group> invalidGroups) {
    if (!invalidGroups.isEmpty()) {
      String invalidGroupNames =
          invalidGroups.stream().map(Group::getName).collect(Collectors.joining(", "));
      throw StateConflictException.inactiveGroup(invalidGroupNames);
    }
  }

  private void validateGroupSelection(List<Group> selectedGroups) {
    if (selectedGroups.isEmpty()) {
      throw ValidationException.groupValidation("No valid groups selected for test assignment");
    }
  }
}
