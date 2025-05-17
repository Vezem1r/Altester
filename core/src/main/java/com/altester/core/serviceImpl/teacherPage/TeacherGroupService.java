package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.subject.GroupsResponse;
import com.altester.core.model.subject.Group;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.serviceImpl.group.GroupActivityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeacherGroupService {

  private final SubjectRepository subjectRepository;
  private final GroupActivityService groupActivityService;
  private final TeacherPageMapper teacherPageMapper;

  /**
   * Filters groups by status (active, inactive, future)
   *
   * @param groups List of groups to filter
   * @param statusFilter Status filter to apply
   * @return Filtered list of groups
   */
  public List<Group> filterGroupsByStatus(List<Group> groups, String statusFilter) {
    if (!StringUtils.hasText(statusFilter)) {
      return groups;
    }

    log.debug("Filtering {} groups by status: {}", groups.size(), statusFilter);
    return groups.stream()
        .filter(
            group -> {
              boolean isInFuture = groupActivityService.isGroupInFuture(group);
              boolean matches =
                  switch (statusFilter.toLowerCase()) {
                    case "active" -> group.isActive() && !isInFuture;
                    case "inactive" -> !group.isActive() && !isInFuture;
                    case "future" -> isInFuture;
                    default -> true;
                  };

              if (matches) {
                log.trace(
                    "Group {} (id: {}) matches status filter '{}'",
                    group.getName(),
                    group.getId(),
                    statusFilter);
              }

              return matches;
            })
        .toList();
  }

  /**
   * Filters groups by name based on search query
   *
   * @param groups List of groups to filter
   * @param searchQuery Search query to apply
   * @return Filtered list of groups
   */
  public List<Group> filterGroupsByName(List<Group> groups, String searchQuery) {
    String searchLower = searchQuery.toLowerCase();
    log.debug("Filtering {} groups by name containing: '{}'", groups.size(), searchQuery);

    return groups.stream()
        .filter(
            group -> {
              boolean matches =
                  group.getName() != null && group.getName().toLowerCase().contains(searchLower);

              if (matches) {
                log.trace(
                    "Group {} (id: {}) matches name search '{}'",
                    group.getName(),
                    group.getId(),
                    searchQuery);
              }

              return matches;
            })
        .toList();
  }

  public List<GroupsResponse> convertGroupsToDTOs(List<Group> groups) {
    return groups.stream()
        .map(
            group -> {
              String subjectName =
                  subjectRepository
                      .findByGroupsContaining(group)
                      .map(
                          subject -> {
                            log.trace("Group belongs to subject: {}", subject.getName());
                            return subject.getShortName() + " " + subject.getName();
                          })
                      .orElse("Unknown Subject");

              boolean isInFuture = groupActivityService.isGroupInFuture(group);

              return teacherPageMapper.toListTeacherGroupDTO(group, subjectName, isInFuture);
            })
        .toList();
  }
}
