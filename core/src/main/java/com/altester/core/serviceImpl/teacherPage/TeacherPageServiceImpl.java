package com.altester.core.serviceImpl.teacherPage;

import com.altester.core.dtos.core_service.TeacherPage.*;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private User getTeacherFromPrincipal(Principal principal) {
        String username = principal.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
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
            return new TeacherPageDTO(teacher.getUsername(), teacher.isRegistered(), List.of());
        }

        Set<Subject> subjects = teacherGroups.stream()
                .map(group -> subjectRepository.findByGroupsContaining(group).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TeacherSubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> {
                    log.trace("Converting subject {} to DTO", subject.getName());
                    return teacherPageMapper.toTeacherSubjectDTO(subject, teacherGroups);
                })
                .collect(Collectors.toList());

        log.info("Successfully prepared teacher page data with {} subjects", subjectDTOs.size());
        return new TeacherPageDTO(teacher.getUsername(), teacher.isRegistered(), subjectDTOs);
    }

    @Override
    @Cacheable(value = "teacherStudents",
            key = "#principal.name + ':page:' + #page + ':size:' + #size + ':search:' + " +
                    "(#searchQuery == null ? '' : #searchQuery)")
    public CacheablePage<TeacherStudentsDTO> getStudents(Principal principal, int page, int size, String searchQuery) {
        log.info("Fetching paginated students list for teacher {} (page: {}, size: {}, search: {})",
                principal.getName(), page, size, searchQuery);

        User teacher = getTeacherFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);

        List<Group> activeGroups = groupRepository.findByTeacher(teacher).stream()
                .filter(Group::isActive)
                .toList();
        log.debug("Found {} active groups", activeGroups.size());

        List<TeacherStudentsDTO> students = getUniqueStudentsWithGroups(activeGroups);
        log.debug("Found {} unique students across all active groups", students.size());

        if (StringUtils.hasText(searchQuery)) {
            students = filterStudentsBySearch(students, searchQuery);
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
            log.debug("Created page with {} students (items {}-{} of {})",
                    pageContent.size(), start, end - 1, students.size());
        }

        Page<TeacherStudentsDTO> result = new PageImpl<>(pageContent, pageable, students.size());
        log.info("Returning page {} of {} with {} students",
                result.getNumber() + 1, result.getTotalPages(), result.getNumberOfElements());

        return new CacheablePage<>(result);
    }

    private List<TeacherStudentsDTO> getUniqueStudentsWithGroups(List<Group> activeGroups) {

        return activeGroups.stream()
                .flatMap(group -> {
                    log.trace("Processing students from group: {} (id: {})", group.getName(), group.getId());
                    return group.getStudents().stream()
                            .collect(Collectors.toMap(
                                    student -> student,
                                    student -> new SubjectGroupDTO(group.getId(), group.getName()),
                                    (existing, replacement) -> existing
                            ))
                            .keySet().stream()
                            .map(student -> {
                                List<SubjectGroupDTO> subjectGroups = activeGroups.stream()
                                        .filter(g -> g.getStudents().contains(student))
                                        .map(g -> new SubjectGroupDTO(g.getId(), g.getName()))
                                        .toList();

                                log.trace("Student {} belongs to {} groups", student.getUsername(), subjectGroups.size());
                                return teacherPageMapper.toTeacherStudentsDTO(student, subjectGroups);
                            });
                })
                .toList();
    }

    private List<TeacherStudentsDTO> filterStudentsBySearch(List<TeacherStudentsDTO> students, String searchQuery) {
        String searchLower = searchQuery.toLowerCase();
        log.debug("Filtering {} students with search term: '{}'", students.size(), searchQuery);

        return students.stream()
                .filter(student ->
                        (student.getFirstName() != null && student.getFirstName().toLowerCase().contains(searchLower)) ||
                                (student.getLastName() != null && student.getLastName().toLowerCase().contains(searchLower)) ||
                                (student.getUsername() != null && student.getUsername().toLowerCase().contains(searchLower))
                )
                .toList();
    }

    @Override
    @Cacheable(value = "teacherGroups",
            key = "#principal.name + ':page:' + #page + ':size:' + #size + ':search:' + " +
                    "(#searchQuery == null ? '' : #searchQuery) + ':status:' + " +
                    "(#statusFilter == null ? '' : #statusFilter)")
    public CacheablePage<ListTeacherGroupDTO> getGroups(Principal principal, int page, int size, String searchQuery, String statusFilter) {
        log.info("Fetching paginated groups list for teacher {} (page: {}, size: {}, search: {}, status: {})",
                principal.getName(), page, size, searchQuery, statusFilter);

        User teacher = getTeacherFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);

        List<Group> teacherGroups = groupRepository.findByTeacher(teacher);
        log.debug("Found {} total groups", teacherGroups.size());

        List<Group> filteredGroups = filterGroupsByStatus(teacherGroups, statusFilter);
        log.debug("{} groups match the status criteria", filteredGroups.size());

        if (StringUtils.hasText(searchQuery)) {
            filteredGroups = filterGroupsByName(filteredGroups, searchQuery);
            log.debug("{} groups match both status and name criteria", filteredGroups.size());
        }

        log.debug("Converting {} filtered groups to DTOs", filteredGroups.size());
        List<ListTeacherGroupDTO> groupDTOs = convertGroupsToDTOs(filteredGroups);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), groupDTOs.size());

        List<ListTeacherGroupDTO> pageContent;
        if (start >= groupDTOs.size()) {
            log.debug("Requested page exceeds available data size, returning empty page");
            pageContent = List.of();
        } else {
            pageContent = groupDTOs.subList(start, end);
            log.debug("Created page with {} groups (items {}-{} of {})",
                    pageContent.size(), start, end - 1, groupDTOs.size());
        }

        Page<ListTeacherGroupDTO> result = new PageImpl<>(pageContent, pageable, groupDTOs.size());
        log.info("Returning page {} of {} with {} groups",
                result.getNumber() + 1, result.getTotalPages(), result.getNumberOfElements());

        return new CacheablePage<>(result);
    }

    private List<Group> filterGroupsByStatus(List<Group> groups, String statusFilter) {
        if (!StringUtils.hasText(statusFilter)) {
            return groups;
        }

        log.debug("Filtering {} groups by status: {}", groups.size(), statusFilter);
        return groups.stream()
                .filter(group -> {
                    boolean isInFuture = groupActivityService.isGroupInFuture(group);
                    boolean matches = switch (statusFilter.toLowerCase()) {
                        case "active" -> group.isActive() && !isInFuture;
                        case "inactive" -> !group.isActive() && !isInFuture;
                        case "future" -> isInFuture;
                        default -> true;
                    };

                    if (matches) {
                        log.trace("Group {} (id: {}) matches status filter '{}'",
                                group.getName(), group.getId(), statusFilter);
                    }

                    return matches;
                })
                .toList();
    }

    private List<Group> filterGroupsByName(List<Group> groups, String searchQuery) {
        String searchLower = searchQuery.toLowerCase();
        log.debug("Filtering {} groups by name containing: '{}'", groups.size(), searchQuery);

        return groups.stream()
                .filter(group -> {
                    boolean matches = group.getName() != null &&
                            group.getName().toLowerCase().contains(searchLower);

                    if (matches) {
                        log.trace("Group {} (id: {}) matches name search '{}'",
                                group.getName(), group.getId(), searchQuery);
                    }

                    return matches;
                })
                .toList();
    }

    private List<ListTeacherGroupDTO> convertGroupsToDTOs(List<Group> groups) {

        return groups.stream()
                .map(group -> {
                    String subjectName = subjectRepository.findByGroupsContaining(group)
                            .map(subject -> {
                                log.trace("Group belongs to subject: {}", subject.getName());
                                return subject.getShortName() + " " + subject.getName();
                            })
                            .orElse("Unknown Subject");

                    boolean isInFuture = groupActivityService.isGroupInFuture(group);

                    return teacherPageMapper.toListTeacherGroupDTO(group, subjectName, isInFuture);
                })
                .toList();
    }

    @Override
    @Transactional
    public void moveStudentBetweenGroups(Principal principal, MoveStudentRequest request) {
        log.info("Processing request to move student {} from group {} to group {}",
                request.getStudentUsername(), request.getFromGroupId(), request.getToGroupId());

        User teacher = getTeacherFromPrincipal(principal);

        try {
            Group fromGroup = groupRepository.findById(request.getFromGroupId())
                    .orElseThrow(() -> {
                        log.error("Source group not found with ID: {}", request.getFromGroupId());
                        return ResourceNotFoundException.group(request.getFromGroupId());
                    });

            Group toGroup = groupRepository.findById(request.getToGroupId())
                    .orElseThrow(() -> {
                        log.error("Target group not found with ID: {}", request.getToGroupId());
                        return ResourceNotFoundException.group(request.getToGroupId());
                    });

            validateGroupsForMove(teacher, fromGroup, toGroup);

            Subject subject = validateSubjectsMatch(fromGroup, toGroup);

            User student = userRepository.findByUsername(request.getStudentUsername())
                    .orElseThrow(() -> {
                        log.error("Student not found with username: {}", request.getStudentUsername());
                        return ResourceNotFoundException.user(request.getStudentUsername(), "Student not found");
                    });

            validateStudentForMove(student, fromGroup, subject, request.getFromGroupId(), teacher);

            fromGroup.getStudents().remove(student);

            toGroup.getStudents().add(student);

            groupRepository.save(fromGroup);
            groupRepository.save(toGroup);

            cacheService.clearTeacherRelatedCaches();
            cacheService.clearStudentRelatedCaches();

            log.info("Successfully moved student {} from group {} to group {}",
                    student.getUsername(), fromGroup.getName(), toGroup.getName());

        } catch (AlTesterException e) {
            log.error("Failed to move student: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while moving student between groups", e);
            throw new RuntimeException("Unexpected error occurred while moving student between groups", e);
        }
    }

    private void validateGroupsForMove(User teacher, Group fromGroup, Group toGroup) {
        boolean isFromGroupInFuture = groupActivityService.isGroupInFuture(fromGroup);
        boolean isToGroupInFuture = groupActivityService.isGroupInFuture(toGroup);

        if ((isFromGroupInFuture && !isToGroupInFuture) || (!isFromGroupInFuture && isToGroupInFuture)) {
            log.error("Source group {} and target group {} are not in same semester", fromGroup.getName(), toGroup.getName());
            throw StateConflictException.differentSemesters(fromGroup.getName(), toGroup.getName());
        }

        boolean isFromGroupActive = groupActivityService.checkAndUpdateGroupActivity(fromGroup);
        boolean isToGroupActive = groupActivityService.checkAndUpdateGroupActivity(toGroup);

        if (!isFromGroupActive && !isFromGroupInFuture) {
            log.error("Source group {} (ID: {}) is inactive and not in the future", fromGroup.getName(), fromGroup.getId());
            throw StateConflictException.inactiveGroup(fromGroup.getName());
        }

        if (!isToGroupActive && !isToGroupInFuture) {
            log.error("Target group {} (ID: {}) is inactive and not in the future", toGroup.getName(), toGroup.getId());
            throw StateConflictException.inactiveGroup(toGroup.getName());
        }

        if (!fromGroup.getTeacher().equals(teacher) || !toGroup.getTeacher().equals(teacher)) {
            log.error("Teacher {} does not own both groups (IDs: {} and {})", teacher.getUsername(), fromGroup.getId(), toGroup.getId());
            throw ValidationException.groupValidation("You can only move students within your own groups");
        }
    }

    private Subject validateSubjectsMatch(Group fromGroup, Group toGroup) {
        Optional<Subject> fromSubject = subjectRepository.findByGroupsContaining(fromGroup);
        Optional<Subject> toSubject = subjectRepository.findByGroupsContaining(toGroup);

        if (fromSubject.isEmpty()) {
            log.error("Source group {} does not belong to any subject", fromGroup.getName());
            throw ValidationException.groupValidation("Source group does not belong to any subject");
        }

        if (toSubject.isEmpty()) {
            log.error("Target group {} does not belong to any subject", toGroup.getName());
            throw ValidationException.groupValidation("Target group does not belong to any subject");
        }

        if (!fromSubject.get().equals(toSubject.get())) {
            log.error("Groups belong to different subjects: {} and {}",
                    fromSubject.get().getName(), toSubject.get().getName());
            throw ValidationException.groupValidation("Groups must belong to the same subject");
        }

        return fromSubject.get();
    }

    private void validateStudentForMove(User student, Group fromGroup, Subject subject, Long fromGroupId, User teacher) {
        if (!fromGroup.getStudents().contains(student)) {
            log.error("Student {} is not in source group {}", student.getUsername(), fromGroup.getName());
            throw ValidationException.groupValidation("Student is not in the source group");
        }

        List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student)
                .stream()
                .filter(g -> subject.getGroups().contains(g))
                .filter(g -> g.getTeacher().equals(teacher))
                .toList();

        log.debug("Student is in {} active groups for subject {}",
                studentActiveGroups.size(), subject.getName());

        if (studentActiveGroups.size() > 1 &&
                studentActiveGroups.stream().anyMatch(g -> g.getId() != fromGroupId)) {
            log.error("Student {} is already in multiple active groups of subject {}",
                    student.getUsername(), subject.getName());
            throw StateConflictException.multipleActiveGroups(
                    "Student is already in multiple active groups of this subject");
        }
    }
}