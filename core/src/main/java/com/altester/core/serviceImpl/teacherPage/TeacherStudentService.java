package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.SubjectGroupDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherStudentsDTO;
import com.altester.core.model.subject.Group;
import java.util.List;
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
    return activeGroups.stream()
        .flatMap(
            group -> {
              log.trace(
                  "Processing students from group: {} (id: {})", group.getName(), group.getId());
              return group.getStudents().stream()
                  .collect(
                      Collectors.toMap(
                          student -> student,
                          student -> new SubjectGroupDTO(group.getId(), group.getName()),
                          (existing, replacement) -> existing))
                  .keySet()
                  .stream()
                  .map(
                      student -> {
                        List<SubjectGroupDTO> subjectGroups =
                            activeGroups.stream()
                                .filter(g -> g.getStudents().contains(student))
                                .map(g -> new SubjectGroupDTO(g.getId(), g.getName()))
                                .toList();

                        log.trace(
                            "Student {} belongs to {} groups",
                            student.getUsername(),
                            subjectGroups.size());
                        return teacherPageMapper.toTeacherStudentsDTO(student, subjectGroups);
                      });
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
