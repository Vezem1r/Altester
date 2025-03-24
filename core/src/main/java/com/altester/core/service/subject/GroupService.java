package com.altester.core.service.subject;

import com.altester.core.config.SemesterConfig;
import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.GroupInactiveException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterConfig semesterConfig;
    private final GroupActivityService groupActivityService;

    public void deleteGroup(long id) {
        try {
            Group group = groupRepository.findById(id).orElseThrow(() -> {
                log.error("Group with id: {} not found", id);
                return new RuntimeException("Group not found");
            });

            if (!groupActivityService.canModifyGroup(group)) {
                throw new GroupInactiveException("Cannot delete inactive group " + group.getName() + " from past semester");
            }

            groupRepository.deleteById(id);
        } catch (GroupInactiveException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting group with id: {}, {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public GroupDTO getGroup(long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        String subjectName = subjectRepository.findByGroupsContaining(group)
                .map(subject -> subject.getShortName() + " " + subject.getName())
                .orElse("Unknown Subject");

        List<GroupUserList> students = group.getStudents().stream()
                .map(student -> new GroupUserList(
                        student.getId(), student.getName(), student.getSurname(), student.getUsername()))
                .toList();

        GroupUserList teacher = (group.getTeacher() != null)
                ? new GroupUserList(
                group.getTeacher().getId(),
                group.getTeacher().getName(),
                group.getTeacher().getSurname(),
                group.getTeacher().getUsername())
                : null;

        boolean isInFuture = groupActivityService.isGroupInFuture(group);

        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .subject(subjectName)
                .students(students)
                .teacher(teacher)
                .semester(group.getSemester())
                .academicYear(group.getAcademicYear())
                .active(group.isActive())
                .isInFuture(isInFuture)
                .build();
    }

    public Page<GroupsResponse> getAllGroups(Pageable pageable, String searchQuery, String activityFilter) {
        List<Group> groups = groupRepository.findAll();

        if (StringUtils.hasText(searchQuery)) {
            String searchLower = searchQuery.toLowerCase();
            groups = groups.stream()
                    .filter(group ->
                            (group.getName() != null && group.getName().toLowerCase().contains(searchLower)) ||
                                    (group.getTeacher() != null && group.getTeacher().getUsername() != null &&
                                            group.getTeacher().getUsername().toLowerCase().contains(searchLower)) ||
                                    (group.getSemester() != null && group.getSemester().toString().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(activityFilter)) {
            groups = groups.stream()
                    .filter(group -> {
                        boolean isInFuture = groupActivityService.isGroupInFuture(group);

                        return switch (activityFilter) {
                            case "active" -> group.isActive() && !isInFuture;
                            case "inactive" -> !group.isActive() && !isInFuture;
                            case "future" -> isInFuture;
                            default -> true;
                        };
                    })
                    .collect(Collectors.toList());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), groups.size());

        if (start > groups.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, groups.size());
        }

        List<Group> pagedGroups = groups.subList(start, end);

        List<GroupsResponse> groupResponses = pagedGroups.stream()
                .map(group -> {
                    Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());
                    String subjectName = subject.isPresent() ? subject.get().getShortName() : "No subject";

                    GroupsResponse response = new GroupsResponse(
                            group.getId(),
                            group.getName(),
                            group.getTeacher() != null ? group.getTeacher().getUsername() : "No teacher",
                            group.getStudents().size(),
                            subjectName,
                            group.getSemester(),
                            group.getAcademicYear(),
                            group.isActive()
                    );

                    boolean isInFuture = groupActivityService.isGroupInFuture(group);
                    response.setInFuture(isInFuture);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(groupResponses, pageable, groups.size());
    }

    @Transactional
    public void updateGroup(Long id, CreateGroupDTO createGroupDTO) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        if (!groupActivityService.canModifyGroup(group)) {
            throw new GroupInactiveException("Cannot update inactive group " + group.getName() + " from past semester");
        }

        if (createGroupDTO.getStudentsIds() == null || createGroupDTO.getStudentsIds().isEmpty()) {
            log.error("Group update failed: At least one student is required");
            throw new IllegalArgumentException("Group must have at least one student");
        }

        if (!group.getName().equals(createGroupDTO.getGroupName()) &&
                groupRepository.findByName(createGroupDTO.getGroupName()).isPresent()) {
            log.error("Group with name '{}' already exists", createGroupDTO.getGroupName());
            throw new IllegalArgumentException("Group with name '" + createGroupDTO.getGroupName() + "' already exists");
        }

        group.setName(createGroupDTO.getGroupName());

        User teacher = userRepository.findById(createGroupDTO.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
            log.error("User with ID '{}' is not a teacher", createGroupDTO.getTeacherId());
            throw new IllegalArgumentException("User is not a teacher");
        }
        group.setTeacher(teacher);

        Set<User> students = createGroupDTO.getStudentsIds().stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getRole().equals(RolesEnum.STUDENT))
                .collect(Collectors.toSet());

        if (students.isEmpty()) {
            log.error("Group update failed: No valid students found");
            throw new IllegalArgumentException("Group update failed: No valid students found");
        }
        group.setStudents(students);

        if (createGroupDTO.getSemester() == null) {
            createGroupDTO.setSemester(Semester.valueOf(semesterConfig.getCurrentSemester()));
        }

        if (createGroupDTO.getAcademicYear() == null) {
            createGroupDTO.setAcademicYear(semesterConfig.getCurrentAcademicYear());
        }

        boolean isActive = semesterConfig.isSemesterActive(createGroupDTO.getSemester().name(), createGroupDTO.getAcademicYear());

        group.setSemester(createGroupDTO.getSemester());
        group.setAcademicYear(createGroupDTO.getAcademicYear());
        group.setActive(isActive);

        groupRepository.save(group);
        log.info("Group '{}' updated successfully with {} students", group.getName(), students.size());
    }

    public Long createGroup(CreateGroupDTO createGroupDTO) {
        try {
            if (groupRepository.findByName(createGroupDTO.getGroupName()).isPresent()) {
                log.error("Group with name '{}' already exists", createGroupDTO.getGroupName());
                throw new IllegalArgumentException("Group with name '" + createGroupDTO.getGroupName() + "' already exists");
            }

            User teacher = userRepository.findById(createGroupDTO.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher cannot be null"));
            if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
                log.error("User with ID '{}' is not a teacher", createGroupDTO.getTeacherId());
                throw new IllegalArgumentException("User is not a teacher");
            }

            if (createGroupDTO.getSemester() == null) {
                createGroupDTO.setSemester(Semester.valueOf(semesterConfig.getCurrentSemester()));
            }

            if (createGroupDTO.getAcademicYear() == null) {
                createGroupDTO.setAcademicYear(semesterConfig.getCurrentAcademicYear());
            }

            log.info("Group semester is: {}", createGroupDTO.getSemester().name());

            boolean isActive = (createGroupDTO.getActive() != null)
                    ? createGroupDTO.getActive()
                    : semesterConfig.isSemesterActive(createGroupDTO.getSemester().name(), createGroupDTO.getAcademicYear());

            log.info("Semester is active: {}", isActive);

            Set<User> students = new HashSet<>();
            if (createGroupDTO.getStudentsIds() != null && !createGroupDTO.getStudentsIds().isEmpty()) {
                students = createGroupDTO.getStudentsIds().stream()
                        .map(userRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(user -> user.getRole().equals(RolesEnum.STUDENT))
                        .collect(Collectors.toSet());
            }

            Group group = Group.builder()
                    .name(createGroupDTO.getGroupName())
                    .teacher(teacher)
                    .students(students)
                    .semester(createGroupDTO.getSemester())
                    .academicYear(createGroupDTO.getAcademicYear())
                    .active(isActive)
                    .build();

            groupRepository.save(group);
            log.info("Group '{}' created successfully with {} students, active status: {}",
                    group.getName(), students.size(), group.isActive());

            return group.getId();
        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating group: " + e.getMessage());
        }
    }

    public Page<CreateGroupUserListDTO> getAllStudents(Pageable pageable, String searchQuery) {
        Page<User> studentsPage;

        if (StringUtils.hasText(searchQuery)) {
            String searchLower = searchQuery.toLowerCase();
            List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);

            List<User> filteredStudents = allStudents.stream()
                    .filter(student -> {
                        String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
                        String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";

                        return fullName.contains(searchLower) || username.contains(searchLower);
                    })
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredStudents.size());

            if (start > filteredStudents.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, filteredStudents.size());
            }

            List<User> pagedStudents = filteredStudents.subList(start, end);
            studentsPage = new PageImpl<>(pagedStudents, pageable, filteredStudents.size());
        } else {
            studentsPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);
        }

        List<CreateGroupUserListDTO> students = studentsPage.getContent().stream()
                .map(student -> {
                    List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

                    List<String> subjectNames = studentActiveGroups.stream()
                            .map(group -> subjectRepository.findByGroupsContaining(group)
                                    .map(Subject::getShortName)
                                    .orElse("Group has no subject"))
                            .distinct()
                            .toList();

                    return new CreateGroupUserListDTO(
                            student.getId(),
                            student.getName(),
                            student.getSurname(),
                            student.getUsername(),
                            subjectNames
                    );
                })
                .toList();

        return new PageImpl<>(students, pageable, studentsPage.getTotalElements());
    }

    public Page<GroupUserList> getAllTeachers(Pageable pageable, String searchQuery) {
        Page<User> teachersPage;

        if (StringUtils.hasText(searchQuery)) {
            List<User> allTeachers = userRepository.findAllByRole(RolesEnum.TEACHER);

            String searchLower = searchQuery.toLowerCase();
            List<User> filteredTeachers = allTeachers.stream()
                    .filter(teacher ->
                            (teacher.getName() != null && teacher.getName().toLowerCase().contains(searchLower)) ||
                                    (teacher.getSurname() != null && teacher.getSurname().toLowerCase().contains(searchLower)) ||
                                    (teacher.getUsername() != null && teacher.getUsername().toLowerCase().contains(searchLower)) ||
                                    (teacher.getEmail() != null && teacher.getEmail().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredTeachers.size());

            if (start > filteredTeachers.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, filteredTeachers.size());
            }

            List<User> pagedTeachers = filteredTeachers.subList(start, end);
            teachersPage = new PageImpl<>(pagedTeachers, pageable, filteredTeachers.size());
        } else {
            teachersPage = userRepository.findByRole(RolesEnum.TEACHER, pageable);
        }

        return teachersPage.map(user -> new GroupUserList(user.getId(), user.getName(), user.getSurname(), user.getUsername()));
    }

    public GroupStudentsResponseDTO getGroupStudentsWithCategories(
            Pageable pageable, Long groupId, String searchQuery, boolean includeCurrentMembers, boolean hideStudentsInSameSubject) {

        if (groupId == null) {
            throw new IllegalArgumentException("Group ID is required");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " not found"));

        List<CreateGroupUserListDTO> currentMembers = group.getStudents().stream()
                .map(student -> {
                    List<String> subjectNames = getStudentSubjects(student);
                    return new CreateGroupUserListDTO(
                            student.getId(),
                            student.getName(),
                            student.getSurname(),
                            student.getUsername(),
                            subjectNames
                    );
                })
                .sorted(Comparator.comparing(dto -> dto.getName() + " " + dto.getSurname()))
                .collect(Collectors.toList());

        Page<CreateGroupUserListDTO> availableStudents =
                includeCurrentMembers ? getAllStudents(pageable, searchQuery) :
                        getAllStudentsNotInGroup(pageable, groupId, searchQuery, hideStudentsInSameSubject);

        return GroupStudentsResponseDTO.builder()
                .currentMembers(currentMembers)
                .availableStudents(availableStudents)
                .build();
    }

    public Page<CreateGroupUserListDTO> getAllStudentsNotInGroup(
            Pageable pageable, Long groupId, String searchQuery, boolean hideStudentsInSameSubject) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " not found"));

        Set<Long> studentsInGroupIds = group.getStudents().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Subject subject;
        Set<Long> studentsInSubjectIds = new HashSet<>();

        Optional<Subject> subjectOptional = subjectRepository.findByGroupsContaining(group);
        if (subjectOptional.isPresent()) {
            subject = subjectOptional.get();

            studentsInSubjectIds = subject.getGroups().stream()
                    .filter(g -> g.getId() != group.getId())
                    .flatMap(g -> g.getStudents().stream())
                    .map(User::getId)
                    .collect(Collectors.toSet());
        } else {
            subject = null;
        }

        List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);
        List<User> filteredStudents;

        if (StringUtils.hasText(searchQuery)) {
            String searchLower = searchQuery.toLowerCase();

            Set<Long> finalStudentsInSubjectIds1 = studentsInSubjectIds;
            filteredStudents = allStudents.stream()
                    .filter(student -> !studentsInGroupIds.contains(student.getId()))
                    .filter(student -> !hideStudentsInSameSubject || !finalStudentsInSubjectIds1.contains(student.getId()))
                    .filter(student -> {
                        String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
                        String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";

                        return fullName.contains(searchLower) || username.contains(searchLower);
                    })
                    .collect(Collectors.toList());
        } else {
            Set<Long> finalStudentsInSubjectIds3 = studentsInSubjectIds;
            filteredStudents = allStudents.stream()
                    .filter(student -> !studentsInGroupIds.contains(student.getId()))
                    .filter(student -> !hideStudentsInSameSubject || !finalStudentsInSubjectIds3.contains(student.getId()))
                    .collect(Collectors.toList());
        }

        if (!studentsInSubjectIds.isEmpty() && !hideStudentsInSameSubject) {
            Set<Long> finalStudentsInSubjectIds2 = studentsInSubjectIds;
            filteredStudents.sort((a, b) -> {
                boolean aInSubject = finalStudentsInSubjectIds2.contains(a.getId());
                boolean bInSubject = finalStudentsInSubjectIds2.contains(b.getId());
                return Boolean.compare(bInSubject, aInSubject);
            });
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredStudents.size());

        Page<User> studentsPage;
        if (start >= filteredStudents.size()) {
            studentsPage = new PageImpl<>(Collections.emptyList(), pageable, filteredStudents.size());
        } else {
            List<User> pagedStudents = filteredStudents.subList(start, end);
            studentsPage = new PageImpl<>(pagedStudents, pageable, filteredStudents.size());
        }

        Set<Long> finalStudentsInSubjectIds = studentsInSubjectIds;
        Subject finalSubject = subject;
        List<CreateGroupUserListDTO> resultList = studentsPage.getContent().stream()
                .map(student -> {
                    List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

                    List<String> subjectNames = studentActiveGroups.stream()
                            .map(g -> subjectRepository.findByGroupsContaining(g)
                                    .map(Subject::getShortName)
                                    .orElse("Group has no subject"))
                            .distinct()
                            .toList();

                    CreateGroupUserListDTO dto = new CreateGroupUserListDTO(
                            student.getId(),
                            student.getName(),
                            student.getSurname(),
                            student.getUsername(),
                            subjectNames
                    );

                    if (finalSubject != null && finalStudentsInSubjectIds.contains(student.getId())) {
                        dto.setInSameSubject(true);
                        dto.setSubjectName(finalSubject.getName());
                        dto.setSubjectShortName(finalSubject.getShortName());
                    }

                    return dto;
                })
                .toList();

        return new PageImpl<>(resultList, pageable, filteredStudents.size());
    }

    private List<String> getStudentSubjects(User student) {
        List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

        return studentActiveGroups.stream()
                .map(group -> subjectRepository.findByGroupsContaining(group)
                        .map(Subject::getShortName)
                        .orElse("Group has no subject"))
                .distinct()
                .collect(Collectors.toList());
    }
}