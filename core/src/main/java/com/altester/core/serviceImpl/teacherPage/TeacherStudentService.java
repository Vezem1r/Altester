package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.SubjectGroupDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherStudentsDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeacherStudentService {

  private final TeacherPageMapper teacherPageMapper;

  /**
   * Extracts unique students with their groups from a list of groups
   *
   * @param activeGroups List of active groups
   * @return List of unique students with their groups
   */
  public List<TeacherStudentsDTO> getUniqueStudentsWithGroups(List<Group> activeGroups) {
    Set<User> uniqueStudents =
        activeGroups.stream()
            .flatMap(group -> group.getStudents().stream())
            .collect(Collectors.toSet());

    log.debug(
        "Found {} unique students across {} active groups",
        uniqueStudents.size(),
        activeGroups.size());

    return uniqueStudents.stream()
        .map(
            student -> {
              List<SubjectGroupDTO> subjectGroups =
                  activeGroups.stream()
                      .filter(group -> group.getStudents().contains(student))
                      .map(
                          group ->
                              new SubjectGroupDTO(group.getId(), group.getName(), group.isActive()))
                      .toList();

              log.trace(
                  "Student {} belongs to {} groups", student.getUsername(), subjectGroups.size());
              return teacherPageMapper.toTeacherStudentsDTO(student, subjectGroups);
            })
        .toList();
  }

  public List<TeacherStudentsDTO> filterStudentsBySearch(
      List<TeacherStudentsDTO> students, String searchQuery) {
    String searchLower = searchQuery.toLowerCase();
    log.debug("Filtering {} students with search term: '{}'", students.size(), searchQuery);

    return students.stream()
        .filter(
            student ->
                (student.getFirstName() != null
                        && student.getFirstName().toLowerCase().contains(searchLower))
                    || (student.getLastName() != null
                        && student.getLastName().toLowerCase().contains(searchLower))
                    || (student.getUsername() != null
                        && student.getUsername().toLowerCase().contains(searchLower)))
        .toList();
  }
}
