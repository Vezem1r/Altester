package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.*;
import com.altester.core.dtos.core_service.TeacherPage.SubjectGroupDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TeacherPageMapper {

  public TeacherSubjectDTO toTeacherSubjectDTO(Subject subject, List<Group> teacherGroups) {
    List<TeacherGroupDTO> groupDTOs =
        teacherGroups.stream()
            .filter(group -> subject.getGroups().contains(group))
            .map(this::toTeacherGroupDTO)
            .collect(Collectors.toList());

    return new TeacherSubjectDTO(
        subject.getName(), subject.getShortName(), subject.getDescription(), groupDTOs);
  }

  public TeacherGroupDTO toTeacherGroupDTO(Group group) {
    return new TeacherGroupDTO(
        group.getName(), group.getStudents().size(), group.getTests().size(), group.isActive());
  }

  public TeacherStudentsDTO toTeacherStudentsDTO(
      User student, List<SubjectGroupDTO> subjectGroups) {
    return new TeacherStudentsDTO(
        student.getName(),
        student.getSurname(),
        student.getEmail(),
        student.getUsername(),
        subjectGroups,
        student.getLastLogin());
  }

  public ListTeacherGroupDTO toListTeacherGroupDTO(
      Group group, String subjectName, boolean isInFuture) {
    List<GroupStudentsDTO> studentDTOs =
        group.getStudents().stream().map(this::toGroupStudentsDTO).toList();

    return new ListTeacherGroupDTO(
        group.getId(), group.getName(), subjectName, studentDTOs, group.isActive(), isInFuture);
  }

  public GroupStudentsDTO toGroupStudentsDTO(User student) {
    return new GroupStudentsDTO(
        student.getUsername(), student.getName(), student.getSurname(), student.getEmail());
  }
}
