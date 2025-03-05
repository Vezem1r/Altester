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
        return groupRepository.findAll(pageable).map(group -> new GroupsResponce(
                group.getId(),
                group.getName(),
                group.getTeacher() != null ? group.getTeacher().getUsername() : "No teacher",
                group.getStudents().size(),
                group.getSubject() != null ? group.getSubject().getName() : "No subject"
        ));
    }

    public void updateGroup(long id, CreateGroupDTO createGroupDTO) {
        try {
            log.info("Attempting to update group with ID: {}", id);

            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Group with ID {} not found", id);
                        return new IllegalArgumentException("Group with ID " + id + " not found");
                    });
            log.info("Group with ID {} found: {}", id, group.getName());

            if (createGroupDTO.getGroupName() != null) {
                group.setName(createGroupDTO.getGroupName());
                log.info("Group name updated to: {}", createGroupDTO.getGroupName());
            }

            User teacher = userRepository.findById(createGroupDTO.getTeacherId())
                    .orElseThrow(() -> {
                        log.error("Teacher with ID {} not found", createGroupDTO.getTeacherId());
                        return new IllegalArgumentException("Teacher cannot be null");
                    });
            if (!teacher.getRole().equals(RolesEnum.TEACHER)) {
                log.error("User with ID '{}' is not a teacher", createGroupDTO.getTeacherId());
                throw new IllegalArgumentException("User is not a teacher");
            }
            group.setTeacher(teacher);
            log.info("Group teacher updated to: {}", teacher.getUsername());

            Subject subject = subjectRepository.findById(createGroupDTO.getSubjectId())
                    .orElseThrow(() -> {
                        log.error("Subject with ID {} not found", createGroupDTO.getSubjectId());
                        return new IllegalArgumentException("Subject cannot be null");
                    });
            group.setSubject(subject);
            log.info("Group subject updated to: {}", subject.getName());

            log.info("Processing student IDs: {}", createGroupDTO.getStudentsIds());

            Set<User> updatedStudents = createGroupDTO.getStudentsIds().stream()
                    .map(studentId -> {
                        Optional<User> studentOpt = userRepository.findById(studentId);
                        if (!studentOpt.isPresent()) {
                            log.warn("Student with ID {} not found", studentId);
                        } else {
                            log.info("Student with ID {} found: {}", studentId, studentOpt.get().getUsername());
                        }
                        return studentOpt;
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(user -> user.getRole().equals(RolesEnum.STUDENT))
                    .collect(Collectors.toSet());

            log.info("Filtered valid students: {}", updatedStudents.size());

            group.getStudents().clear();
            log.info("Removed all existing students from the group.");

            if (updatedStudents.isEmpty()) {
                log.error("Update failed: Group must have at least one new student with role STUDENT");
                throw new IllegalArgumentException("Group must have at least one student with role STUDENT");
            }

            group.setStudents(updatedStudents);
            groupRepository.save(group);
            log.info("Group '{}' updated successfully with {} new students", group.getName(), updatedStudents.size());

        } catch (Exception e) {
            log.error("Error updating group with ID {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
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

            Subject subject = subjectRepository.findById(createGroupDTO.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject cannot be null"));

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
                    .subject(subject)
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