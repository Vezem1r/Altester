package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.*;
import com.altester.core.dtos.core_service.TeacherPage.SubjectGroupDTO;
import com.altester.core.dtos.core_service.subject.GroupsResponse;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherPageMapper {

  private final SubjectRepository subjectRepository;

  public TeacherSubjectDTO toTeacherSubjectDTO(Subject subject, List<Group> teacherGroups) {
    List<TeacherGroupDTO> groupDTOs =
        teacherGroups.stream()
            .filter(group -> subject.getGroups().contains(group))
            .map(this::toTeacherGroupDTO)
            .toList();

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

  public GroupsResponse toListTeacherGroupDTO(Group group, String subjectName, boolean isInFuture) {

    Subject subject = subjectRepository.findByGroupsContaining(group).orElse(null);

    String shortName;

    if (subject == null) {
      shortName = "No specific subject";
    } else {
      shortName = subject.getShortName();
    }

    return new GroupsResponse(
        group.getId(),
        group.getName(),
        subjectName,
        group.getStudents().size(),
        shortName,
        group.getSemester(),
        group.getAcademicYear(),
        group.isActive(),
        isInFuture);
  }

  public GroupStudentsDTO toGroupStudentsDTO(User student) {
    return new GroupStudentsDTO(
        student.getUsername(), student.getName(), student.getSurname(), student.getEmail());
  }
}
