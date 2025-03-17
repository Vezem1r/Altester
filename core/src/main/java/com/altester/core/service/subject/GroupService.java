package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public void deleteGroup(long id) {
        try {
            groupRepository.findById(id).orElseThrow(() -> {
                log.error("Group with id: {} not found", id);
                return new RuntimeException("Group not found");
            });
            groupRepository.deleteById(id);
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

        GroupUserList teacher = new GroupUserList(
                group.getTeacher().getId(),
                group.getTeacher().getName(),
                group.getTeacher().getSurname(),
                group.getTeacher().getUsername()
        );

        return new GroupDTO(group.getId(), group.getName(), subjectName, students, teacher);
    }

    public Page<GroupsResponce> getAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(group -> {
            Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());
            String subjectName;
            if (subject.isPresent()) {
                subjectName = subject.get().getShortName();
            } else {
                subjectName = "No subject";
            }
            return new GroupsResponce(
                    group.getId(),
                    group.getName(),
                    group.getTeacher() != null ? group.getTeacher().getUsername() : "No teacher",
                    group.getStudents().size(),
                    subjectName
            );
        });
    }

    @Transactional
    public void updateGroup(Long id, CreateGroupDTO createGroupDTO) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

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
                    .build();

            groupRepository.save(group);
            log.info("Group '{}' created successfully with {} students", group.getName(), students.size());

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
                    List<String> subjectNames = groupRepository.findAll().stream()
                            .filter(group -> group.getStudents().contains(student))
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

    public Page<GroupUserList> getAllTeachers(Pageable pageable) {
        return userRepository.findAllByRole(RolesEnum.TEACHER, pageable)
                .map(user -> new GroupUserList(user.getId(), user.getName(), user.getSurname(), user.getUsername()));
    }
}
