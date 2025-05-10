package com.altester.core.serviceImpl.group;

import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupFilterService {

  private final GroupActivityService groupActivityService;
  private final SubjectRepository subjectRepository;

  public List<Group> applySearchFilter(List<Group> groups, String searchQuery) {
    if (!StringUtils.hasText(searchQuery)) return groups;

    String lowerQuery = searchQuery.toLowerCase();
    return groups.stream()
        .filter(
            group ->
                (group.getName() != null && group.getName().toLowerCase().contains(lowerQuery))
                    || (group.getTeacher() != null
                        && group.getTeacher().getUsername() != null
                        && group.getTeacher().getUsername().toLowerCase().contains(lowerQuery))
                    || (group.getSemester() != null
                        && group.getSemester().toString().toLowerCase().contains(lowerQuery)))
        .toList();
  }

  public List<Group> applyActivityFilter(List<Group> groups, String activityFilter) {
    if (!StringUtils.hasText(activityFilter)) return groups;

    return groups.stream()
        .filter(
            group -> {
              boolean isInFuture = groupActivityService.isGroupInFuture(group);
              return switch (activityFilter) {
                case "active" -> group.isActive() && !isInFuture;
                case "inactive" -> !group.isActive() && !isInFuture;
                case "future" -> isInFuture;
                default -> true;
              };
            })
        .toList();
  }

  public List<Group> applyAvailabilityAndSubjectFilter(
      List<Group> groups, Boolean available, Long subjectId) {
    if (available != null && available) {
      Set<Long> groupsInSubjects =
          subjectRepository.findAll().stream()
              .flatMap(subject -> subject.getGroups().stream())
              .map(Group::getId)
              .collect(Collectors.toSet());

      return groups.stream()
          .filter(group -> !groupsInSubjects.contains(group.getId()))
          .filter(group -> group.isActive() || groupActivityService.isGroupInFuture(group))
          .toList();
    } else if (subjectId != null) {
      Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
      if (subjectOpt.isPresent()) {
        Set<Long> groupIds =
            subjectOpt.get().getGroups().stream().map(Group::getId).collect(Collectors.toSet());

        return groups.stream()
            .filter(group -> groupIds.contains(group.getId()))
            .filter(group -> group.isActive() || groupActivityService.isGroupInFuture(group))
            .toList();
      } else {
        log.warn("Subject with ID {} not found", subjectId);
        return new ArrayList<>();
      }
    }

    return groups;
  }
}
