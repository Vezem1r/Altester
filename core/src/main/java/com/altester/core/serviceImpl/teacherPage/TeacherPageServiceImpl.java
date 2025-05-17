package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.*;
import com.altester.core.dtos.core_service.subject.GroupsResponse;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.TeacherPageService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.group.GroupActivityService;
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
import org.springframework.transaction.annotation.Transactional;
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
  private final TeacherStudentMoveValidator moveValidator;

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

    if (teacherGroups.isEmpty()) {
      log.debug("No groups found for teacher {}, returning empty page", teacher.getUsername());
      return new TeacherPageDTO(
          teacher.getUsername(),
          teacher.getName(),
          teacher.getSurname(),
          teacher.getEmail(),
          teacher.isRegistered(),
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
  @Transactional
  public void moveStudentBetweenGroups(Principal principal, MoveStudentRequest request) {
    log.info(
        "Processing request to move student {} from group {} to group {}",
        request.getStudentUsername(),
        request.getFromGroupId(),
        request.getToGroupId());

    User teacher = getTeacherFromPrincipal(principal);

    try {
      Group fromGroup =
          groupRepository
              .findById(request.getFromGroupId())
              .orElseThrow(
                  () -> {
                    log.error("Source group not found with ID: {}", request.getFromGroupId());
                    return ResourceNotFoundException.group(request.getFromGroupId());
                  });

      Group toGroup =
          groupRepository
              .findById(request.getToGroupId())
              .orElseThrow(
                  () -> {
                    log.error("Target group not found with ID: {}", request.getToGroupId());
                    return ResourceNotFoundException.group(request.getToGroupId());
                  });

      moveValidator.validateGroupsForMove(teacher, fromGroup, toGroup);

      Subject subject = moveValidator.validateSubjectsMatch(fromGroup, toGroup);

      User student =
          userRepository
              .findByUsername(request.getStudentUsername())
              .orElseThrow(
                  () -> {
                    log.error("Student not found with username: {}", request.getStudentUsername());
                    return ResourceNotFoundException.user(
                        request.getStudentUsername(), "Student not found");
                  });

      moveValidator.validateStudentForMove(
          student, fromGroup, subject, request.getFromGroupId(), teacher);

      fromGroup.getStudents().remove(student);
      toGroup.getStudents().add(student);

      groupRepository.save(fromGroup);
      groupRepository.save(toGroup);

      cacheService.clearTeacherRelatedCaches();
      cacheService.clearStudentRelatedCaches();

      log.info(
          "Successfully moved student {} from group {} to group {}",
          student.getUsername(),
          fromGroup.getName(),
          toGroup.getName());

    } catch (AlTesterException e) {
      log.error("Failed to move student: {} - {}", e.getClass().getSimpleName(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error occurred while moving student between groups", e);
      throw new RuntimeException(
          "Unexpected error occurred while moving student between groups", e);
    }
  }
}
