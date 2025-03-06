package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateGroupDTO;
import com.altester.core.dtos.core_service.subject.GroupsResponce;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Group getGroup(long id) {
        try {
            return groupRepository.findById(id).orElseThrow(() -> {
                log.error("Group with id: {} not found", id);
                return new RuntimeException("Group not found");
            });
        } catch (Exception e) {
            log.error("Error retrieving group with id: {}, {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Page<GroupsResponce> getAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(group -> {
            Optional<Subject> subject = subjectRepository.findByGroupsId(group.getId());
            String subjectName;
            if (subject.isPresent()) {
                subjectName = subject.get().getName();
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

    public void createGroup(CreateGroupDTO createGroupDTO) {
        try {
            if (createGroupDTO.getStudentsIds() == null || createGroupDTO.getStudentsIds().isEmpty()) {
                log.error("Group creation failed: At least one student is required");
                throw new IllegalArgumentException("Group must have at least one student");
            }

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

            Set<User> students = createGroupDTO.getStudentsIds().stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(user -> user.getRole().equals(RolesEnum.STUDENT))
                    .collect(Collectors.toSet());

            if (students.isEmpty()) {
                log.error("Group creation failed: No valid students found");
                throw new IllegalArgumentException("Group creation failed: No valid students found");
            }

            Group group = Group.builder()
                    .name(createGroupDTO.getGroupName())
                    .teacher(teacher)
                    .students(students)
                    .build();

            groupRepository.save(group);
            log.info("Group '{}' created successfully with {} students", group.getName(), students.size());

        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
