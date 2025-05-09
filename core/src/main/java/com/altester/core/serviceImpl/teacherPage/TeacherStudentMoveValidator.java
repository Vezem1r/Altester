package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.exception.StateConflictException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.serviceImpl.group.GroupActivityService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeacherStudentMoveValidator {

  private final SubjectRepository subjectRepository;
  private final GroupRepository groupRepository;
  private final GroupActivityService groupActivityService;

  /**
   * Validates that the groups are eligible for student movement
   *
   * @param teacher The teacher user
   * @param fromGroup Source group
   * @param toGroup Target group
   * @throws StateConflictException if groups are in different semesters or inactive
   * @throws ValidationException if teacher doesn't own both groups
   */
  public void validateGroupsForMove(User teacher, Group fromGroup, Group toGroup) {
    boolean isFromGroupInFuture = groupActivityService.isGroupInFuture(fromGroup);
    boolean isToGroupInFuture = groupActivityService.isGroupInFuture(toGroup);

    if ((isFromGroupInFuture && !isToGroupInFuture)
        || (!isFromGroupInFuture && isToGroupInFuture)) {
      log.error(
          "Source group {} and target group {} are not in same semester",
          fromGroup.getName(),
          toGroup.getName());
      throw StateConflictException.differentSemesters(fromGroup.getName(), toGroup.getName());
    }

    boolean isFromGroupActive = groupActivityService.checkAndUpdateGroupActivity(fromGroup);
    boolean isToGroupActive = groupActivityService.checkAndUpdateGroupActivity(toGroup);

    if (!isFromGroupActive && !isFromGroupInFuture) {
      log.error(
          "Source group {} (ID: {}) is inactive and not in the future",
          fromGroup.getName(),
          fromGroup.getId());
      throw StateConflictException.inactiveGroup(fromGroup.getName());
    }

    if (!isToGroupActive && !isToGroupInFuture) {
      log.error(
          "Target group {} (ID: {}) is inactive and not in the future",
          toGroup.getName(),
          toGroup.getId());
      throw StateConflictException.inactiveGroup(toGroup.getName());
    }

    if (!fromGroup.getTeacher().equals(teacher) || !toGroup.getTeacher().equals(teacher)) {
      log.error(
          "Teacher {} does not own both groups (IDs: {} and {})",
          teacher.getUsername(),
          fromGroup.getId(),
          toGroup.getId());
      throw ValidationException.groupValidation(
          "You can only move students within your own groups");
    }
  }

  /**
   * Validates that the source and target groups belong to the same subject
   *
   * @param fromGroup Source group
   * @param toGroup Target group
   * @return The subject both groups belong to
   * @throws ValidationException if groups don't belong to the same subject
   */
  public Subject validateSubjectsMatch(Group fromGroup, Group toGroup) {
    Optional<Subject> fromSubject = subjectRepository.findByGroupsContaining(fromGroup);
    Optional<Subject> toSubject = subjectRepository.findByGroupsContaining(toGroup);

    if (fromSubject.isEmpty()) {
      log.error("Source group {} does not belong to any subject", fromGroup.getName());
      throw ValidationException.groupValidation("Source group does not belong to any subject");
    }

    if (toSubject.isEmpty()) {
      log.error("Target group {} does not belong to any subject", toGroup.getName());
      throw ValidationException.groupValidation("Target group does not belong to any subject");
    }

    if (!fromSubject.get().equals(toSubject.get())) {
      log.error(
          "Groups belong to different subjects: {} and {}",
          fromSubject.get().getName(),
          toSubject.get().getName());
      throw ValidationException.groupValidation("Groups must belong to the same subject");
    }

    return fromSubject.get();
  }

  /**
   * Validates that the student can be moved from one group to another
   *
   * @param student The student to move
   * @param fromGroup Source group
   * @param subject The subject both groups belong to
   * @param fromGroupId Source group ID
   * @param teacher The teacher user
   * @throws ValidationException if student is not in the source group
   * @throws StateConflictException if student is already in multiple active groups
   */
  public void validateStudentForMove(
      User student, Group fromGroup, Subject subject, Long fromGroupId, User teacher) {
    if (!fromGroup.getStudents().contains(student)) {
      log.error("Student {} is not in source group {}", student.getUsername(), fromGroup.getName());
      throw ValidationException.groupValidation("Student is not in the source group");
    }

    List<Group> studentActiveGroups =
        groupRepository.findByStudentsContainingAndActiveTrue(student).stream()
            .filter(g -> subject.getGroups().contains(g))
            .filter(g -> g.getTeacher().equals(teacher))
            .toList();

    log.debug(
        "Student is in {} active groups for subject {}",
        studentActiveGroups.size(),
        subject.getName());

    if (studentActiveGroups.size() > 1
        && studentActiveGroups.stream().anyMatch(g -> g.getId() != fromGroupId)) {
      log.error(
          "Student {} is already in multiple active groups of subject {}",
          student.getUsername(),
          subject.getName());
      throw StateConflictException.multipleActiveGroups(
          "Student is already in multiple active groups of this subject");
    }
  }
}
