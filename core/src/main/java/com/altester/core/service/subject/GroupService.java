package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateGroupDTO;
import com.altester.core.dtos.core_service.subject.GroupsResponce;
import com.altester.core.model.auth.User;
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
            log.error("Error deleting group with id: {}, {}",id, e.getMessage());
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
            log.error("Error retrieving group with id: {}, {}",id, e.getMessage());
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
            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Group with ID " + id + " not found"));

            if (createGroupDTO.getGroupName() != null) {
                group.setName(createGroupDTO.getGroupName());
            }

            User teacher = userRepository.findById(createGroupDTO.getTeacherId()).orElse(null);
            group.setTeacher(teacher);

            Subject subject = subjectRepository.findById(createGroupDTO.getSubjectId()).orElse(null);
            group.setSubject(subject);

            Set<User> updatedStudents = createGroupDTO.getStudentsIds().stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            if (updatedStudents.isEmpty()) {
                log.error("Update failed: Group must have at least one student");
                throw new IllegalArgumentException("Group must have at least one student");
            }

            group.setStudents(updatedStudents);
            groupRepository.save(group);
            log.info("Group '{}' updated successfully", group.getName());

        } catch (Exception e) {
            log.error("Error updating group with id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error updating group: " + e.getMessage());
        }
    }

    public void createGroup(CreateGroupDTO createGroupDTO) {
        try {
            if (createGroupDTO.getStudentsIds().isEmpty()) {
                log.error("Group creation failed: At least one student is required");
                throw new IllegalArgumentException("Group must have at least one student");
            }

            if (groupRepository.findByName(createGroupDTO.getGroupName()).isPresent()) {
                log.error("Group with name '{}' already exists", createGroupDTO.getGroupName());
                throw new IllegalArgumentException("Group with name '" + createGroupDTO.getGroupName() + "' already exists");
            }

            User teacher = userRepository.findById(createGroupDTO.getTeacherId()).orElse(null);
            Subject subject = subjectRepository.findById(createGroupDTO.getSubjectId()).orElse(null);

            Set<User> students = createGroupDTO.getStudentsIds().stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
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
            throw new RuntimeException("Error creating group: " + e.getMessage());
        }
    }
}
