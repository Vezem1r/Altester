package com.altester.core.serviceImpl.group;

import com.altester.core.config.SemesterConfig;
import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.GroupService;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.CacheablePage;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final SubjectRepository subjectRepository;
  private final SemesterConfig semesterConfig;
  private final GroupActivityService groupActivityService;
  private final GroupDTOMapper groupMapper;
  private final NotificationDispatchService notificationService;
  private final GroupFilterService groupsFilter;
  private final GroupStudentService studentService;
  private final GroupPaginationUtils paginationUtils;
  private final CacheService cacheService;

  private Group getGroupById(long id) {
    return groupRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.error("Group with id: {} not found", id);
              return ResourceNotFoundException.group(id);
            });
  }

  @Override
  @Cacheable(value = "group", key = "'id:' + #id")
  public GroupDTO getGroup(long id) {
    Group group = getGroupById(id);

    String subjectName =
        subjectRepository
            .findByGroupsContaining(group)
            .map(subject -> subject.getShortName() + " " + subject.getName())
            .orElse("Unknown Subject");

    boolean isInFuture = groupActivityService.isGroupInFuture(group);

    return groupMapper.toGroupDTO(group, subjectName, isInFuture);
  }

  @Override
  @Cacheable(
      value = "groups",
      key =
          "'page:' + #page + ':size:' + #size + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':activity:' +"
              + "(#activityFilter == null ? '' : #activityFilter) + ':available:' +"
              + "(#available == null ? 'false' : #available) +"
              + "':subject:' + (#subjectId == null ? '0' : #subjectId)")
  public CacheablePage<GroupsResponse> getAllGroups(
      int page,
      int size,
      String searchQuery,
      String activityFilter,
      Boolean available,
      Long subjectId) {

    Pageable pageable = PageRequest.of(page, size);
    List<Group> groups = groupRepository.findAll();

    groups = groupsFilter.applySearchFilter(groups, searchQuery);
    groups = groupsFilter.applyActivityFilter(groups, activityFilter);
    groups = groupsFilter.applyAvailabilityAndSubjectFilter(groups, available, subjectId);

    return paginationUtils.paginateAndMapGroups(groups, pageable);
  }

  @Override
  public CacheablePage<CreateGroupUserListDTO> getAllStudents(
      int page, int size, String searchQuery) {
    return studentService.getAllStudents(page, size, searchQuery);
  }

  @Override
  public GroupStudentsResponseDTO getGroupStudentsWithCategories(
      int page, int size, Long groupId, String searchQuery, boolean includeCurrentMembers) {
    return studentService.getGroupStudentsWithCategories(
        page, size, groupId, searchQuery, includeCurrentMembers);
  }

  @Override
  public CacheablePage<CreateGroupUserListDTO> getAllStudentsNotInGroup(
      int page, int size, Long groupId, String searchQuery) {
    return studentService.getAllStudentsNotInGroup(page, size, groupId, searchQuery);
  }

  @Override
  @Cacheable(
      value = "groupTeachers",
      key =
          "'page:' + #page + ':size:' + #size + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
  public CacheablePage<GroupUserList> getAllTeachers(int page, int size, String searchQuery) {
    Pageable pageable = PageRequest.of(page, size);

    Page<User> teachersPage;

    if (StringUtils.hasText(searchQuery)) {
      List<User> allTeachers = userRepository.findAllByRole(RolesEnum.TEACHER);

      String searchLower = searchQuery.toLowerCase();
      List<User> filteredTeachers =
          allTeachers.stream()
              .filter(
                  teacher ->
                      (teacher.getName() != null
                              && teacher.getName().toLowerCase().contains(searchLower))
                          || (teacher.getSurname() != null
                              && teacher.getSurname().toLowerCase().contains(searchLower))
                          || (teacher.getUsername() != null
                              && teacher.getUsername().toLowerCase().contains(searchLower))
                          || (teacher.getEmail() != null
                              && teacher.getEmail().toLowerCase().contains(searchLower)))
              .toList();

      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), filteredTeachers.size());

      if (start > filteredTeachers.size()) {
        Page<GroupUserList> emptyPage =
            new PageImpl<>(Collections.emptyList(), pageable, filteredTeachers.size());
        return new CacheablePage<>(emptyPage);
      }

      List<User> pagedTeachers = filteredTeachers.subList(start, end);
      teachersPage = new PageImpl<>(pagedTeachers, pageable, filteredTeachers.size());
    } else {
      teachersPage = userRepository.findByRole(RolesEnum.TEACHER, pageable);
    }

    Page<GroupUserList> resultPage = teachersPage.map(groupMapper::toGroupUserList);
    return new CacheablePage<>(resultPage);
  }
}
