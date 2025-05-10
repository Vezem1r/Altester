package com.altester.core.serviceImpl.group;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.util.CacheablePage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupPaginationUtils {

  private final SubjectRepository subjectRepository;
  private final GroupDTOMapper groupMapper;
  private final GroupActivityService groupActivityService;

  public CacheablePage<GroupsResponse> paginateAndMapGroups(List<Group> groups, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), groups.size());

    if (start >= groups.size()) {
      return new CacheablePage<>(new PageImpl<>(Collections.emptyList(), pageable, groups.size()));
    }

    List<Group> pagedGroups = groups.subList(start, end);
    List<GroupsResponse> responses =
        pagedGroups.stream()
            .map(
                group -> {
                  Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());
                  String subjectName = subject.map(Subject::getShortName).orElse("No subject");
                  boolean isInFuture = groupActivityService.isGroupInFuture(group);
                  return groupMapper.toGroupsResponse(group, subjectName, isInFuture);
                })
            .toList();

    return new CacheablePage<>(new PageImpl<>(responses, pageable, groups.size()));
  }
}
