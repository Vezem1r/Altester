package com.altester.core.serviceImpl.group;

import com.altester.core.config.SemesterConfig;
import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.GroupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class GroupServiceImpl  implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterConfig semesterConfig;
    private final GroupActivityService groupActivityService;
    private final GroupDTOMapper groupMapper;

    private Group getGroupById(long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Group with id: {} not found", id);
                    return ResourceNotFoundException.group(id);
                });
    }

    private User getUserById(long id, String role) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
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
            log.info("Group with id {} successfully deleted", id);
        } catch (Exception e) {
            log.error("Error deleting group with id: {}, {}", id, e.getMessage());
            throw ValidationException.groupValidation("Error deleting group: " + e.getMessage());
        }
    }

    @Override
    public GroupDTO getGroup(long id) {
        Group group = getGroupById(id);

        String subjectName = subjectRepository.findByGroupsContaining(group)
                .map(subject -> subject.getShortName() + " " + subject.getName())
                .orElse("Unknown Subject");

        boolean isInFuture = groupActivityService.isGroupInFuture(group);

        return groupMapper.toGroupDTO(group, subjectName, isInFuture);
    }

    @Override
    public Page<GroupsResponse> getAllGroups(int page, int size, String searchQuery, String activityFilter,
                                             Boolean available, Long subjectId) {
        Pageable pageable = PageRequest.of(page, size);

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

        if (available != null && available) {
            List<Subject> allSubjects = subjectRepository.findAll();
            Set<Long> groupsInSubjects = allSubjects.stream()
                    .flatMap(subject -> subject.getGroups().stream())
                    .map(Group::getId)
                    .collect(Collectors.toSet());

            groups = groups.stream()
                    .filter(group -> !groupsInSubjects.contains(group.getId()))
                    .filter(group -> group.isActive() || groupActivityService.isGroupInFuture(group))
                    .collect(Collectors.toList());
        } else if (subjectId != null) {
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
            if (subjectOpt.isPresent()) {
                Subject subject = subjectOpt.get();
                Set<Long> groupsIds = subject.getGroups().stream()
                        .map(Group::getId)
                        .collect(Collectors.toSet());

                groups = groups.stream()
                        .filter(group -> groupsIds.contains(group.getId()))
                        .filter(group -> group.isActive() || groupActivityService.isGroupInFuture(group))
                        .collect(Collectors.toList());
            } else {
                log.warn("Subject with ID {} not found", subjectId);
                groups = new ArrayList<>();
            }
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

                    boolean isInFuture = groupActivityService.isGroupInFuture(group);
                    return groupMapper.toGroupsResponse(group, subjectName, isInFuture);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(groupResponses, pageable, groups.size());
    }

    @Override
    @Transactional
    public void updateGroup(Long id, CreateGroupDTO createGroupDTO) {
        Group group = getGroupById(id);

        if (!groupActivityService.canModifyGroup(group)) {
            log.error("Cannot update inactive group {} from past semester", group.getName());
            throw StateConflictException.inactiveGroup(group.getName());
        }

        if (createGroupDTO.getStudentsIds() == null || createGroupDTO.getStudentsIds().isEmpty()) {
            log.error("Group update failed: At least one student is required");
            throw ValidationException.groupValidation("Group must have at least one student");
        }

        if (!group.getName().equals(createGroupDTO.getGroupName()) &&
                groupRepository.findByName(createGroupDTO.getGroupName()).isPresent()) {
            log.error("Group with name '{}' already exists", createGroupDTO.getGroupName());
            throw ResourceAlreadyExistsException.group(createGroupDTO.getGroupName());
        }

        group.setName(createGroupDTO.getGroupName());

        User teacher = getUserById(createGroupDTO.getTeacherId(), "Teacher");
        if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
            log.error("User with ID '{}' is not a teacher", createGroupDTO.getTeacherId());
            throw StateConflictException.roleConflict("User is not a teacher");
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
            throw ValidationException.groupValidation("Group update failed: No valid students found");
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

        if (createGroupDTO.getSemester() == null) {
            createGroupDTO.setSemester(Semester.valueOf(semesterConfig.getCurrentSemester()));
        }

        if (createGroupDTO.getAcademicYear() == null) {
            createGroupDTO.setAcademicYear(semesterConfig.getCurrentAcademicYear());
        }

        boolean isActive = (createGroupDTO.getActive() != null)
                ? createGroupDTO.getActive()
                : semesterConfig.isSemesterActive(createGroupDTO.getSemester().name(), createGroupDTO.getAcademicYear());

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

        Group savedGroup = groupRepository.save(group);
        log.info("Group '{}' created successfully with {} students, active status: {}",
                group.getName(), students.size(), group.isActive());

        return savedGroup.getId();
    }

    @Override
    public Page<CreateGroupUserListDTO> getAllStudents(int page, int size, String searchQuery) {

        Pageable pageable = PageRequest.of(page, size);

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

                    return groupMapper.toCreateGroupUserListDTO(student, subjectNames);
                })
                .toList();

        return new PageImpl<>(students, pageable, studentsPage.getTotalElements());
    }

    @Override
    public Page<GroupUserList> getAllTeachers(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size);

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

        return teachersPage.map(groupMapper::toGroupUserList);
    }

    @Override
    public GroupStudentsResponseDTO getGroupStudentsWithCategories(
            int page, int size, Long groupId, String searchQuery, boolean includeCurrentMembers, boolean hideStudentsInSameSubject) {

        if (groupId == null) {
            throw ValidationException.invalidParameter("groupId", "Group ID is required");
        }

        Group group = getGroupById(groupId);

        List<String> subjectNames = group.getStudents().stream()
                .flatMap(student -> getStudentSubjects(student).stream())
                .distinct()
                .collect(Collectors.toList());

        List<CreateGroupUserListDTO> currentMembers = groupMapper.mapAndSortCurrentMembers(
                group.getStudents(), subjectNames);

        Page<CreateGroupUserListDTO> availableStudents =
                includeCurrentMembers ? getAllStudents(page, size, searchQuery) :
                        getAllStudentsNotInGroup(page, size, groupId, searchQuery, hideStudentsInSameSubject);

        return GroupStudentsResponseDTO.builder()
                .currentMembers(currentMembers)
                .availableStudents(availableStudents)
                .build();
    }

    @Override
    public Page<CreateGroupUserListDTO> getAllStudentsNotInGroup(
            int page, int size, Long groupId, String searchQuery, boolean hideStudentsInSameSubject) {

        Pageable pageable = PageRequest.of(page, size);

        Group group = getGroupById(groupId);

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

                    CreateGroupUserListDTO dto = groupMapper.toCreateGroupUserListDTO(student, subjectNames);

                    if (finalSubject != null && finalStudentsInSubjectIds.contains(student.getId())) {
                        groupMapper.enrichWithSubjectInfo(
                                dto,
                                true,
                                finalSubject.getName(),
                                finalSubject.getShortName()
                        );
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