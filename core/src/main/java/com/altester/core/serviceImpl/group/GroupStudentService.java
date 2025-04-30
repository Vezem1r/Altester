package com.altester.core.serviceImpl.group;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupStudentService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final GroupDTOMapper groupMapper;
    private final GroupActivityService groupActivityService;

    public Group getGroupById(long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Group with id: {} not found", id);
                    return ResourceNotFoundException.group(id);
                });
    }

    /**
     * Retrieves a paginated list of students with optional search filtering.
     * Supports caching to improve performance for repeated queries.
     *
     * @param page The page number of results to retrieve (zero-indexed)
     * @param size The number of students per page
     * @param searchQuery Optional text to filter students by name or username
     * @return A pageable collection of student DTOs matching the search criteria
     */
    @Cacheable(
            value = "groupStudents",
            key = "'page:' + #page + ':size:' + #size + ':search:' + (#searchQuery == null ? '' : #searchQuery)"
    )
    public CacheablePage<CreateGroupUserListDTO> getAllStudents(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> studentsPage = getFilteredStudentsPage(pageable, searchQuery);

        List<CreateGroupUserListDTO> studentDTOs = studentsPage.getContent().stream()
                .map(this::mapStudentToDTO)
                .toList();

        Page<CreateGroupUserListDTO> resultPage = new PageImpl<>(studentDTOs, pageable, studentsPage.getTotalElements());
        return new CacheablePage<>(resultPage);
    }

    /**
     * Retrieves a filtered page of students, considering search query and role.
     * Used internally to support student pagination and filtering.
     *
     * @param pageable Pagination information
     * @param searchQuery Optional text to filter students
     * @return A page of User entities matching the search criteria
     */
    private Page<User> getFilteredStudentsPage(Pageable pageable, String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return userRepository.findByRole(RolesEnum.STUDENT, pageable);
        }

        String searchLower = searchQuery.toLowerCase();
        List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);

        List<User> filtered = allStudents.stream()
                .filter(student -> matchesSearch(student, searchLower))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        if (start >= filtered.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, filtered.size());
        }

        List<User> paged = filtered.subList(start, end);
        return new PageImpl<>(paged, pageable, filtered.size());
    }

    /**
     * Checks if a student matches the search criteria.
     *
     * @param student The student to check
     * @param searchLower The lowercase search query
     * @return true if the student matches the search, false otherwise
     */
    private boolean matchesSearch(User student, String searchLower) {
        String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
        String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";
        return fullName.contains(searchLower) || username.contains(searchLower);
    }

    /**
     * Maps a student to a Data Transfer Object (DTO) with additional group and subject information.
     *
     * @param student The student to map
     * @return A CreateGroupUserListDTO containing student details and associated subject names
     */
    private CreateGroupUserListDTO mapStudentToDTO(User student) {
        List<Group> activeGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);
        return groupMapper.toCreateGroupUserListDTO(student);
    }

    /**
     * Retrieves group students with categorization, supporting pagination and optional filtering.
     *
     * @param page The page number of results to retrieve (zero-indexed)
     * @param size The number of students per page
     * @param groupId The unique identifier of the group
     * @param searchQuery Optional text to filter students
     * @param includeCurrentMembers Flag to determine whether to include current group members in available students
     * @return A response containing current group members and available students
     * @throws ValidationException if group ID is not provided
     */
    @Cacheable(value = "groupStudentsWithCategories",
            key = "'page:' + #page + ':size:' + #size + ':groupId:' + #groupId + ':search:' + (#searchQuery == null ? '' : #searchQuery) + ':includeMembers:' + #includeCurrentMembers")
    public GroupStudentsResponseDTO getGroupStudentsWithCategories(
            int page, int size, Long groupId, String searchQuery, boolean includeCurrentMembers) {

        if (groupId == null) {
            throw ValidationException.invalidParameter("groupId", "Group ID is required");
        }

        Group group = getGroupById(groupId);

        List<CreateGroupUserListDTO> currentMembers = groupMapper.mapAndSortCurrentMembers(
                group.getStudents());

        CacheablePage<CreateGroupUserListDTO> availableStudents =
                includeCurrentMembers ?
                        getAllStudents(page, size, searchQuery) :
                        getAllStudentsNotInGroup(page, size, groupId, searchQuery);

        return GroupStudentsResponseDTO.builder()
                .currentMembers(currentMembers)
                .availableStudents(availableStudents)
                .build();
    }

    /**
     * Retrieves a paginated list of students not currently in a specific group.
     * Supports caching and advanced filtering based on subject and group constraints.
     *
     * @param page The page number of results to retrieve (zero-indexed)
     * @param size The number of students per page
     * @param groupId The unique identifier of the group
     * @param searchQuery Optional text to filter students
     * @return A pageable collection of student DTOs not in the specified group
     * @throws ResourceNotFoundException if the group does not exist
     */
    @Cacheable(
            value = "groupStudentsNotInGroup",
            key = "'page:' + #page + ':size:' + #size + ':groupId:' + #groupId +" +
                    "':search:' + (#searchQuery == null ? '' : #searchQuery)")
    public CacheablePage<CreateGroupUserListDTO> getAllStudentsNotInGroup(
            int page, int size, Long groupId, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size);

        Group group = getGroupById(groupId);
        Set<Long> studentsInGroupIds = group.getStudents().stream().map(User::getId).collect(Collectors.toSet());

        Subject subject = subjectRepository.findByGroupsContaining(group).orElse(null);

        boolean isGroupInFuture = groupActivityService.isGroupInFuture(group);

        Set<Long> relevantStudentIds = getRelevantStudentIds(subject, group, isGroupInFuture);

        List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);

        List<User> filteredStudents = filterStudents(
                allStudents, studentsInGroupIds, relevantStudentIds, searchQuery
        );

        List<User> pagedStudents = paginateStudents(filteredStudents, pageable);
        List<CreateGroupUserListDTO> dtoList = convertToDto(pagedStudents, relevantStudentIds, subject);

        return new CacheablePage<>(new PageImpl<>(dtoList, pageable, filteredStudents.size()));
    }

    /**
     * Determines relevant student IDs based on subject, current group, and group activity status.
     *
     * @param subject The subject associated with the group
     * @param currentGroup The current group being processed
     * @param isCurrentGroupInFuture Flag indicating if the current group is a future group
     * @return A set of student IDs relevant to the specified conditions
     */
    public Set<Long> getRelevantStudentIds(Subject subject, Group currentGroup, boolean isCurrentGroupInFuture) {
        if (subject == null) return Collections.emptySet();

        Semester currentSemester = currentGroup.getSemester();
        int currentAcademicYear = currentGroup.getAcademicYear();

        if (!isCurrentGroupInFuture) {
            return subject.getGroups().stream()
                    .filter(g -> g.getId() != currentGroup.getId())
                    .filter(Group::isActive)
                    .flatMap(g -> g.getStudents().stream())
                    .map(User::getId)
                    .collect(Collectors.toSet());
        } else {
            return subject.getGroups().stream()
                    .filter(g -> g.getId() != currentGroup.getId())
                    .filter(groupActivityService::isGroupInFuture)
                    .filter(g -> g.getSemester() == currentSemester &&
                            g.getAcademicYear().equals(currentAcademicYear))
                    .flatMap(g -> g.getStudents().stream())
                    .map(User::getId)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Filters students based on group membership, subject constraints, and optional search query.
     *
     * @param allStudents The complete list of students to filter
     * @param studentsInGroupIds Set of student IDs already in the group
     * @param studentsInSameSubjectIds Set of student IDs in related subject groups
     * @param searchQuery Optional text to filter students
     * @return A filtered list of students
     */
    public List<User> filterStudents(
            List<User> allStudents,
            Set<Long> studentsInGroupIds,
            Set<Long> studentsInSameSubjectIds,
            String searchQuery
    ) {
        return allStudents.stream()
                .filter(student -> !studentsInGroupIds.contains(student.getId()))
                .filter(student -> !studentsInSameSubjectIds.contains(student.getId()))
                .filter(student -> {
                    if (!StringUtils.hasText(searchQuery)) return true;

                    String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
                    String username = Optional.ofNullable(student.getUsername()).orElse("").toLowerCase();
                    return fullName.contains(searchQuery.toLowerCase()) || username.contains(searchQuery.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    /**
     * Paginates a list of students based on the provided pagination information.
     *
     * @param students The complete list of students
     * @param pageable Pagination information
     * @return A sublist of students for the specified page
     */
    public List<User> paginateStudents(List<User> students, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), students.size());

        if (start >= students.size()) return Collections.emptyList();
        return students.subList(start, end);
    }

    /**
     * Converts a list of students to DTOs with optional subject-related enrichment.
     *
     * @param students The list of students to convert
     * @param studentsInSubjectIds Set of student IDs in the subject
     * @param subject The subject associated with the group (can be null)
     * @return A list of student DTOs with additional information
     */
    public List<CreateGroupUserListDTO> convertToDto(
            List<User> students,
            Set<Long> studentsInSubjectIds,
            Subject subject
    ) {
        return students.stream().map(student -> {
            List<Group> activeGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

            CreateGroupUserListDTO dto = groupMapper.toCreateGroupUserListDTO(student);

            if (subject != null && studentsInSubjectIds.contains(student.getId())) {
                groupMapper.enrichWithSubjectInfo(dto, true, subject.getName(), subject.getShortName());
            }

            return dto;
        }).toList();
    }

    /**
     * Retrieves student IDs for other groups in the same subject.
     *
     * @param subject The subject to search within
     * @param currentGroup The current group to exclude
     * @return A set of student IDs in other active groups of the same subject
     */
    public Set<Long> getOtherGroupsStudentIds(Subject subject, Group currentGroup) {
        if (subject == null) return Collections.emptySet();

        return subject.getGroups().stream()
                .filter(g -> g.getId() != currentGroup.getId())
                .filter(Group::isActive)
                .flatMap(g -> g.getStudents().stream())
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the list of subjects a student is enrolled in.
     *
     * @param student The student to retrieve subjects for
     * @return A list of subject short names
     */
    public List<String> getStudentSubjects(User student) {
        List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

        return studentActiveGroups.stream()
                .map(group -> subjectRepository.findByGroupsContaining(group)
                        .map(Subject::getShortName)
                        .orElse("Group has no subject"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Validates that students are not already in other active groups of the same subject,
     * or in future groups of the same semester and year if the current group is a future group
     *
     * @param students Students to validate
     * @param subject Subject which holds this group
     * @param currentGroupId Current group ID (can be null for new groups)
     * @throws ValidationException if validation fails
     */
    public void validateStudents(Set<User> students, Subject subject, Long currentGroupId) {
        if (subject == null) {
            return;
        }

        Group currentGroup = currentGroupId != null
                ? groupRepository.findById(currentGroupId).orElse(null)
                : null;

        if (currentGroup == null) {
            validateAgainstActiveGroups(students, subject, currentGroupId);
            return;
        }

        boolean isCurrentGroupInFuture = groupActivityService.isGroupInFuture(currentGroup);

        if (isCurrentGroupInFuture) {
            validateAgainstFutureGroups(students, subject, currentGroup);
        } else {
            validateAgainstActiveGroups(students, subject, currentGroupId);
        }
    }

    /**
     * Validates students against active groups to prevent multiple enrollments in the same subject.
     *
     * @param students The set of students to validate
     * @param subject The subject to check against
     * @param currentGroupId The ID of the current group
     * @throws ValidationException if a student is already in an active group of the same subject
     */
    private void validateAgainstActiveGroups(Set<User> students, Subject subject, Long currentGroupId) {
        Set<Group> otherActiveGroups = subject.getGroups().stream()
                .filter(Group::isActive)
                .filter(g -> currentGroupId == null || g.getId() != currentGroupId)
                .collect(Collectors.toSet());

        if (otherActiveGroups.isEmpty()) {
            return;
        }

        for (User student : students) {
            for (Group group : otherActiveGroups) {
                if (group.getStudents().contains(student)) {
                    log.error("Student {} is already in another active group {} of the same subject",
                            student.getUsername(), group.getName());
                    throw ValidationException.groupValidation(
                            "Student " + student.getName() + " " + student.getSurname() +
                                    " is already in active group '" + group.getName() +
                                    "' of the same subject. Students cannot be in multiple active groups of the same subject.");
                }
            }
        }
    }

    /**
     * Validates students against future groups to prevent multiple enrollments in the same subject and semester.
     *
     * @param students The set of students to validate
     * @param subject The subject to check against
     * @param currentGroup The current group being processed
     * @throws ValidationException if a student is already in a future group of the same subject and semester
     */
    private void validateAgainstFutureGroups(Set<User> students, Subject subject, Group currentGroup) {
        Semester currentSemester = currentGroup.getSemester();
        int currentAcademicYear = currentGroup.getAcademicYear();

        Set<Group> otherFutureGroups = subject.getGroups().stream()
                .filter(g -> g.getId() != currentGroup.getId())
                .filter(groupActivityService::isGroupInFuture)
                .filter(g -> g.getSemester() == currentSemester && g.getAcademicYear() == currentAcademicYear)
                .collect(Collectors.toSet());

        if (otherFutureGroups.isEmpty()) {
            return;
        }

        for (User student : students) {
            for (Group group : otherFutureGroups) {
                if (group.getStudents().contains(student)) {
                    log.error("Student {} is already in another future group {} of the same subject for semester {} and year {}",
                            student.getUsername(), group.getName(), currentSemester, currentAcademicYear);
                    throw ValidationException.groupValidation(
                            "Student " + student.getName() + " " + student.getSurname() +
                                    " is already in future group '" + group.getName() +
                                    "' of the same subject for semester " + currentSemester +
                                    " and year " + currentAcademicYear +
                                    ". Students cannot be in multiple future groups of the same subject for the same semester and year.");
                }
            }
        }
    }

    /**
     * Validates students for a specific semester and academic year to prevent multiple enrollments.
     *
     * @param students The set of students to validate
     * @param subject The subject to check against
     * @param currentGroupId The ID of the current group
     * @param semester The semester to validate
     * @param academicYear The academic year to validate
     * @throws ValidationException if a student is already in a group of the same subject for the specified semester and year
     */
    public void validateStudentsForSemesterAndYear(
            Set<User> students,
            Subject subject,
            Long currentGroupId,
            Semester semester,
            Integer academicYear) {

        if (subject == null) {
            return;
        }

        Set<Group> groupsInSemesterAndYear = subject.getGroups().stream()
                .filter(g -> currentGroupId == null || g.getId() != currentGroupId)
                .filter(g -> g.getSemester() == semester && g.getAcademicYear().equals(academicYear))
                .collect(Collectors.toSet());

        if (groupsInSemesterAndYear.isEmpty()) {
            return;
        }

        List<User> studentsWithIssues = new ArrayList<>();
        Map<User, Group> studentGroupMap = new HashMap<>();

        for (User student : students) {
            for (Group group : groupsInSemesterAndYear) {
                if (group.getStudents().contains(student)) {
                    studentsWithIssues.add(student);
                    studentGroupMap.put(student, group);
                    break;
                }
            }
        }

        if (!studentsWithIssues.isEmpty()) {
            User firstStudent = studentsWithIssues.getFirst();
            Group firstStudentGroup = studentGroupMap.get(firstStudent);

            String errorMessage;
            if (studentsWithIssues.size() == 1) {
                errorMessage = "Student " + firstStudent.getName() + " " + firstStudent.getSurname() +
                        " is already in another group '" + firstStudentGroup.getName() +
                        "' of the same subject for semester " + semester +
                        " and year " + academicYear +
                        ". Students cannot be in multiple future groups of the same subject for the same semester and year.";
            } else {
                errorMessage = "Student " + firstStudent.getName() + " " + firstStudent.getSurname() +
                        " and " + (studentsWithIssues.size() - 1) + " more students are already in other groups " +
                        "of the same subject for semester " + semester +
                        " and year " + academicYear +
                        ". Students cannot be in multiple future groups of the same subject for the same semester and year.";
            }

            log.error("Found {} students already in other groups of the subject for semester {} and year {}",
                    studentsWithIssues.size(), semester, academicYear);

            throw ValidationException.groupValidation(errorMessage);
        }
    }
}