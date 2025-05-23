package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.*;
import com.altester.core.dtos.core_service.subject.GroupUserList;
import com.altester.core.dtos.core_service.subject.GroupsResponse;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.TeacherPageService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.group.GroupActivityService;
import com.altester.core.serviceImpl.group.GroupDTOMapper;
import com.altester.core.util.AiAccuracy;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
public class TeacherPageServiceImpl implements TeacherPageService {

  private final CacheService cacheService;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final GroupActivityService groupActivityService;
  private final TeacherPageMapper teacherPageMapper;
  private final TeacherGroupService teacherGroupService;
  private final TeacherStudentService teacherStudentService;
  private final GroupDTOMapper groupDTOMapper;
  private final AiAccuracy aiAccuracy;
  private final TestRepository testRepository;

  private User getTeacherFromPrincipal(Principal principal) {
    String username = principal.getName();

    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> {
              log.error("Teacher not found with username: {}", username);
              return ResourceNotFoundException.user(username, "Teacher not found: " + username);
            });
  }

  @Override
  @Cacheable(value = "teacherPage", key = "#principal.name")
  public TeacherPageDTO getPage(Principal principal) {
    log.info("Fetching teacher page data for {}", principal.getName());
    User teacher = getTeacherFromPrincipal(principal);

    List<Group> teacherGroups = groupRepository.findByTeacher(teacher);

    int amountOfStudents = teacherStudentService.getUniqueStudentsWithGroups(teacherGroups).size();
    int testCount =
        groupRepository.findAllByTeacher(teacher).stream()
            .flatMap(group -> group.getTests().stream())
            .collect(Collectors.toSet())
            .size();

    if (teacherGroups.isEmpty()) {
      log.debug("No groups found for teacher {}, returning empty page", teacher.getUsername());
      return new TeacherPageDTO(
          teacher.getUsername(),
          teacher.getName(),
          teacher.getSurname(),
          teacher.getEmail(),
          teacher.isRegistered(),
          aiAccuracy.calculateAiAccuracy(),
          amountOfStudents,
          testCount,
          List.of());
    }

    Set<Subject> subjects =
        teacherGroups.stream()
            .map(group -> subjectRepository.findByGroupsContaining(group).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<TeacherSubjectDTO> subjectDTOs =
        subjects.stream()
            .map(
                subject -> {
                  log.trace("Converting subject {} to DTO", subject.getName());
                  return teacherPageMapper.toTeacherSubjectDTO(subject, teacherGroups);
                })
            .toList();

    log.info("Successfully prepared teacher page data with {} subjects", subjectDTOs.size());
    return new TeacherPageDTO(
        teacher.getUsername(),
        teacher.getName(),
        teacher.getSurname(),
        teacher.getEmail(),
        teacher.isRegistered(),
        aiAccuracy.calculateAiAccuracy(),
        amountOfStudents,
        testCount,
        subjectDTOs);
  }

  @Override
  @Cacheable(
      value = "teacherStudents",
      key =
          "#principal.name + ':page:' + #page + ':size:' + #size + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery)")
  public CacheablePage<TeacherStudentsDTO> getStudents(
      Principal principal, int page, int size, String searchQuery) {
    log.info(
        "Fetching paginated students list for teacher {} (page: {}, size: {}, search: {})",
        principal.getName(),
        page,
        size,
        searchQuery);

    User teacher = getTeacherFromPrincipal(principal);
    Pageable pageable = PageRequest.of(page, size);

    List<Group> activeGroups =
        groupRepository.findByTeacher(teacher).stream().filter(Group::isActive).toList();
    log.debug("Found {} active groups", activeGroups.size());

    List<TeacherStudentsDTO> students =
        teacherStudentService.getUniqueStudentsWithGroups(activeGroups);
    log.debug("Found {} unique students across all active groups", students.size());

    if (StringUtils.hasText(searchQuery)) {
      students = teacherStudentService.filterStudentsBySearch(students, searchQuery);
      log.debug("{} students match the search criteria", students.size());
    }

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), students.size());

    List<TeacherStudentsDTO> pageContent;
    if (start >= students.size()) {
      log.debug("Requested page exceeds available data size, returning empty page");
      pageContent = List.of();
    } else {
      pageContent = students.subList(start, end);
      log.debug(
          "Created page with {} students (items {}-{} of {})",
          pageContent.size(),
          start,
          end - 1,
          students.size());
    }

    Page<TeacherStudentsDTO> result = new PageImpl<>(pageContent, pageable, students.size());
    log.info(
        "Returning page {} of {} with {} students",
        result.getNumber() + 1,
        result.getTotalPages(),
        result.getNumberOfElements());

    return new CacheablePage<>(result);
  }

  @Override
  @Cacheable(
      value = "teacherGroups",
      key =
          "#principal.name + ':page:' + #page + ':size:' + #size + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':status:' + "
              + "(#statusFilter == null ? '' : #statusFilter)")
  public CacheablePage<GroupsResponse> getGroups(
      Principal principal, int page, int size, String searchQuery, String statusFilter) {
    log.info(
        "Fetching paginated groups list for teacher {} (page: {}, size: {}, search: {}, status: {})",
        principal.getName(),
        page,
        size,
        searchQuery,
        statusFilter);

    User teacher = getTeacherFromPrincipal(principal);
    Pageable pageable = PageRequest.of(page, size);

    List<Group> teacherGroups = groupRepository.findByTeacher(teacher);
    log.debug("Found {} total groups", teacherGroups.size());

    List<Group> filteredGroups =
        teacherGroupService.filterGroupsByStatus(teacherGroups, statusFilter);
    log.debug("{} groups match the status criteria", filteredGroups.size());

    if (StringUtils.hasText(searchQuery)) {
      filteredGroups = teacherGroupService.filterGroupsByName(filteredGroups, searchQuery);
      log.debug("{} groups match both status and name criteria", filteredGroups.size());
    }

    log.debug("Converting {} filtered groups to DTOs", filteredGroups.size());
    List<GroupsResponse> groupDTOs = teacherGroupService.convertGroupsToDTOs(filteredGroups);

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), groupDTOs.size());

    List<GroupsResponse> pageContent;
    if (start >= groupDTOs.size()) {
      log.debug("Requested page exceeds available data size, returning empty page");
      pageContent = List.of();
    } else {
      pageContent = groupDTOs.subList(start, end);
      log.debug(
          "Created page with {} groups (items {}-{} of {})",
          pageContent.size(),
          start,
          end - 1,
          groupDTOs.size());
    }

    Page<GroupsResponse> result = new PageImpl<>(pageContent, pageable, groupDTOs.size());
    log.info(
        "Returning page {} of {} with {} groups",
        result.getNumber() + 1,
        result.getTotalPages(),
        result.getNumberOfElements());

    return new CacheablePage<>(result);
  }

  @Override
  @Cacheable(value = "teacherGroup", key = "#principal.name + ':' + #groupId")
  public TeacherGroupDetailDTO getTeacherGroup(Principal principal, Long groupId) {
    log.info("Fetching group details for teacher {} and group ID {}", principal.getName(), groupId);

    User teacher = getTeacherFromPrincipal(principal);

    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> {
                  log.error("Group not found with ID: {}", groupId);
                  return ResourceNotFoundException.group(groupId);
                });

    if (!group.getTeacher().equals(teacher)) {
      log.error(
          "Access denied: Teacher {} does not teach group {}",
          teacher.getUsername(),
          group.getName());
      throw ValidationException.groupValidation("You do not have permission to view this group");
    }

    Subject subject =
        subjectRepository
            .findByGroupsContaining(group)
            .orElseThrow(
                () -> {
                  log.error("Subject not found for group with ID: {}", groupId);
                  return ResourceNotFoundException.subject("Subject not found for group");
                });

    String subjectName = subject.getShortName() + " " + subject.getName();

    boolean isInFuture = groupActivityService.isGroupInFuture(group);

    List<TeacherOtherGroupDTO> otherGroups =
        subject.getGroups().stream()
            .filter(g -> g.getTeacher().equals(teacher))
            .filter(g -> g.getId() != group.getId())
            .filter(Group::isActive)
            .map(g -> new TeacherOtherGroupDTO(g.getId(), g.getName()))
            .toList();

    List<GroupUserList> studentsList =
        group.getStudents().stream().map(groupDTOMapper::toGroupUserList).toList();

    return TeacherGroupDetailDTO.builder()
        .id(group.getId())
        .name(group.getName())
        .subject(subjectName)
        .students(studentsList)
        .semester(group.getSemester())
        .academicYear(group.getAcademicYear())
        .active(group.isActive())
        .isInFuture(isInFuture)
        .otherTeacherGroups(otherGroups)
        .build();
  }
}
