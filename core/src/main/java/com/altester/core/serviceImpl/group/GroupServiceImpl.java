package com.altester.core.serviceImpl.group;

import com.altester.core.config.SemesterConfig;
import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.GroupService;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.CacheablePage;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
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

  private User getUserById(long id, String role) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.error("{} with id: {} not found", role, id);
              return ResourceNotFoundException.user(String.valueOf(id), role + " not found");
            });
  }

  @Override
  @Transactional
  public void deleteGroup(long id) {
    Group group = getGroupById(id);

    if (!groupActivityService.canModifyGroup(group)) {
      log.error("Cannot delete inactive group {} from past semester", group.getName());
      throw StateConflictException.inactiveGroup(group.getName());
    }

    try {
      groupRepository.deleteById(id);

      cacheService.clearAllCaches();

      log.info("Group with id {} successfully deleted", id);
    } catch (Exception e) {
      log.error("Error deleting group with id: {}, {}", id, e.getMessage());
      throw ValidationException.groupValidation("Error deleting group: " + e.getMessage());
    }
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
  @Transactional
  public void updateGroup(Long id, UpdateGroupDTO updateGroupDTO) {
    Group group = getGroupById(id);

    Set<User> originalStudents = new HashSet<>(group.getStudents());

    if (!groupActivityService.canModifyGroup(group)) {
      log.error("Cannot update inactive group {} from past semester", group.getName());
      throw StateConflictException.inactiveGroup(group.getName());
    }

    if (updateGroupDTO.getStudentsIds() == null || updateGroupDTO.getStudentsIds().isEmpty()) {
      log.error("Group update failed: At least one student is required");
      throw ValidationException.groupValidation("Group must have at least one student");
    }

    if (!group.getName().equals(updateGroupDTO.getGroupName())
        && groupRepository.findByName(updateGroupDTO.getGroupName()).isPresent()) {
      log.error("Group with name '{}' already exists", updateGroupDTO.getGroupName());
      throw ResourceAlreadyExistsException.group(updateGroupDTO.getGroupName());
    }

    group.setName(updateGroupDTO.getGroupName());

    User teacher = getUserById(updateGroupDTO.getTeacherId(), "Teacher");
    if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
      log.error("User with ID '{}' is not a teacher", updateGroupDTO.getTeacherId());
      throw StateConflictException.roleConflict("User is not a teacher");
    }
    group.setTeacher(teacher);

    Set<User> students =
        updateGroupDTO.getStudentsIds().stream()
            .map(userRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(user -> user.getRole().equals(RolesEnum.STUDENT))
            .collect(Collectors.toSet());

    if (students.isEmpty()) {
      log.error("Group update failed: No valid students found");
      throw ValidationException.groupValidation("Group update failed: No valid students found");
    }

    Subject subject = subjectRepository.findByGroupsContaining(group).orElse(null);

    if (updateGroupDTO.getSemester() == null) {
      updateGroupDTO.setSemester(semesterConfig.getCurrentSemester());
    }

    if (updateGroupDTO.getAcademicYear() == null) {
      updateGroupDTO.setAcademicYear(semesterConfig.getCurrentAcademicYear());
    }

    boolean isSemesterOrYearChanging =
        !group.getSemester().equals(updateGroupDTO.getSemester())
            || !group.getAcademicYear().equals(updateGroupDTO.getAcademicYear());

    studentService.validateStudents(students, subject, group.getId());

    if (isSemesterOrYearChanging && subject != null) {
      studentService.validateStudentsForSemesterAndYear(
          students,
          subject,
          group.getId(),
          updateGroupDTO.getSemester(),
          updateGroupDTO.getAcademicYear());
    }

    group.setStudents(students);

    boolean isActive =
        semesterConfig.isSemesterActive(
            updateGroupDTO.getSemester(), updateGroupDTO.getAcademicYear());
    group.setSemester(updateGroupDTO.getSemester());
    group.setAcademicYear(updateGroupDTO.getAcademicYear());
    group.setActive(isActive);

    groupRepository.save(group);

    cacheService.clearGroupRelatedCaches();
    cacheService.clearStudentRelatedCaches();
    cacheService.clearTeacherRelatedCaches();

    log.info("Group '{}' updated successfully with {} students", group.getName(), students.size());

    Set<User> newStudents =
        students.stream()
            .filter(student -> !originalStudents.contains(student))
            .collect(Collectors.toSet());

    if (!newStudents.isEmpty()) {
      newStudents.forEach(student -> notificationService.notifyNewStudentJoined(student, group));
    }
  }

  @Override
  @Transactional
  public Long createGroup(CreateGroupDTO createGroupDTO) {
    if (groupRepository.findByName(createGroupDTO.getGroupName()).isPresent()) {
      log.error("Group with name '{}' already exists", createGroupDTO.getGroupName());
      throw ResourceAlreadyExistsException.group(createGroupDTO.getGroupName());
    }

    User teacher = getUserById(createGroupDTO.getTeacherId(), "Teacher");
    if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
      log.error("User with ID '{}' is not a teacher", createGroupDTO.getTeacherId());
      throw StateConflictException.roleConflict("User is not a teacher");
    }

    if (createGroupDTO.getSubjectId() == null) {
      log.error("Subject ID is required when creating a group");
      throw ValidationException.invalidParameter(
          "subject", "Subject is required when creating a group");
    }

    Subject subject =
        subjectRepository
            .findById(createGroupDTO.getSubjectId())
            .orElseThrow(
                () -> {
                  log.error("Subject with ID {} not found", createGroupDTO.getSubjectId());
                  return ResourceNotFoundException.subject(createGroupDTO.getSubjectId());
                });

    if (createGroupDTO.getSemester() == null) {
      createGroupDTO.setSemester(semesterConfig.getCurrentSemester());
    }

    if (createGroupDTO.getAcademicYear() == null) {
      createGroupDTO.setAcademicYear(semesterConfig.getCurrentAcademicYear());
    }

    boolean isActive =
        (createGroupDTO.getActive() != null)
            ? createGroupDTO.getActive()
            : semesterConfig.isSemesterActive(
                createGroupDTO.getSemester(), createGroupDTO.getAcademicYear());

    Group group =
        Group.builder()
            .name(createGroupDTO.getGroupName())
            .teacher(teacher)
            .students(new HashSet<>())
            .semester(createGroupDTO.getSemester())
            .academicYear(createGroupDTO.getAcademicYear())
            .active(isActive)
            .build();

    Group savedGroup = groupRepository.save(group);

    subject.getGroups().add(savedGroup);
    subjectRepository.save(subject);

    cacheService.clearAllCaches();

    log.info(
        "Group '{}' created successfully, active status: {}", group.getName(), group.isActive());

    return savedGroup.getId();
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
