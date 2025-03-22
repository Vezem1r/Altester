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

    public Page<GroupsResponse> getAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(group -> {
            Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());
            String subjectName;
            if (subject.isPresent()) {
                subjectName = subject.get().getShortName();
            } else {
                subjectName = "No subject";
            }

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
        });
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

    public Page<CreateGroupUserListDTO> getAllStudents(Pageable pageable) {
        Page<User> studentsPage = userRepository.findAllByRole(RolesEnum.STUDENT, pageable);

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

    public Page<CreateGroupUserListDTO> getAllStudentsSortedBySubject(Pageable pageable, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject with id " + subjectId + " not found"));

        Set<Long> studentsInSubjectIds = new HashSet<>();

        Set<Group> activeSubjectGroups = subject.getGroups().stream()
                .filter(Group::isActive)
                .collect(Collectors.toSet());

        activeSubjectGroups.forEach(group -> group.getStudents().forEach(student -> studentsInSubjectIds.add(student.getId())));

        Page<User> studentsPage = userRepository.findAllByRoleOrderBySubjectMembership(
                RolesEnum.STUDENT.name(),
                studentsInSubjectIds,
                pageable
        );

        List<CreateGroupUserListDTO> resultList = studentsPage.getContent().stream()
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

        return new PageImpl<>(resultList, pageable, studentsPage.getTotalElements());
    }

    public Page<GroupUserList> getAllTeachers(Pageable pageable) {
        return userRepository.findAllByRole(RolesEnum.TEACHER, pageable)
                .map(user -> new GroupUserList(user.getId(), user.getName(), user.getSurname(), user.getUsername()));
    }

    public Page<CreateGroupUserListDTO> getAllStudentsNotInGroup(Pageable pageable, Long groupId) {
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

        if (studentsInGroupIds.isEmpty() && studentsInSubjectIds.isEmpty()) {
            return getAllStudents(pageable);
        }

        Page<User> studentsPage = userRepository.findAllByRoleExcludeGroupStudentsOrderBySubjectMembership(
                RolesEnum.STUDENT.name(),
                studentsInGroupIds,
                studentsInSubjectIds,
                pageable
        );

        Set<Long> finalStudentsInSubjectIds = studentsInSubjectIds;
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

                    if (subject != null && finalStudentsInSubjectIds.contains(student.getId())) {
                        dto.setInSameSubject(true);
                        dto.setSubjectName(subject.getName());
                        dto.setSubjectShortName(subject.getShortName());
                    }

                    return dto;
                })
                .toList();

        return new PageImpl<>(resultList, pageable, studentsPage.getTotalElements());
    }
}