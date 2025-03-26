package com.altester.core.service.TeacherPage;

import com.altester.core.dtos.TeacherPage.*;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.subject.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherPageService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final GroupActivityService groupActivityService;

    public TeacherPageDTO getPage(String username) {
        User teacher = userRepository.findByUsername(username).orElseThrow(()
                -> new UserNotFoundException("Teacher not found" + username));

        List<Group> teacherGroups = groupRepository.findByTeacher(teacher);

        if (teacherGroups.isEmpty()) {
            return new TeacherPageDTO(username, List.of());
        }

        Set<Subject> subjects = teacherGroups.stream()
                .map(group -> subjectRepository.findByGroupsContaining(group).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TeacherSubjectDTO> subjectDTOS = subjects.stream()
                .map(subject -> new TeacherSubjectDTO(
                        subject.getName(),
                        subject.getShortName(),
                        subject.getDescription(),
                        teacherGroups.stream()
                                .filter(group -> subject.getGroups().contains(group))
                                .map(group -> new TeacherGroupDTO(
                                        group.getName(),
                                        group.getStudents().size(),
                                        group.getTests().size(),
                                        group.isActive()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return new TeacherPageDTO(username, subjectDTOS);
    }

    public Page<TeacherStudentsDTO> getStudents(String username, Pageable pageable) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Teacher not found: " + username));

        List<Group> activeGroups = groupRepository.findByTeacher(teacher).stream()
                .filter(Group::isActive)
                .toList();

        List<TeacherStudentsDTO> students = activeGroups.stream()
                .flatMap(group -> group.getStudents().stream()
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

                            return new TeacherStudentsDTO(
                                    student.getName(),
                                    student.getSurname(),
                                    student.getEmail(),
                                    student.getUsername(),
                                    subjectGroups,
                                    student.getLastLogin()
                            );
                        }))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), students.size());
        List<TeacherStudentsDTO> pageContent = students.subList(start, end);

        return new PageImpl<>(pageContent, pageable, students.size());
    }


    public Page<ListTeacherGroupDTO> getGroups(String username, Pageable pageable) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Teacher not found: " + username));

        List<ListTeacherGroupDTO> groups = groupRepository.findByTeacher(teacher).stream()
                .map(group -> {
                    String subjectName = subjectRepository.findByGroupsContaining(group)
                            .map(Subject::getName)
                            .orElse("Unknown Subject");

                    List<GroupStudentsDTO> students = group.getStudents().stream()
                            .map(student -> new GroupStudentsDTO(
                                    student.getUsername(),
                                    student.getName(),
                                    student.getSurname(),
                                    student.getEmail()))
                            .toList();

                    return new ListTeacherGroupDTO(
                            group.getId(),
                            group.getName(),
                            subjectName,
                            students,
                            group.isActive());
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), groups.size());
        List<ListTeacherGroupDTO> pageContent = groups.subList(start, end);

        return new PageImpl<>(pageContent, pageable, groups.size());
    }

    @Transactional
    public void moveStudentBetweenGroups(String username, String studentUsername, Long fromGroupId, Long toGroupId) {
        try {
            User teacher = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Teacher not found: " + username));

            Group fromGroup = groupRepository.findById(fromGroupId)
                    .orElseThrow(() -> new GroupNotFoundException("Source group not found"));
            Group toGroup = groupRepository.findById(toGroupId)
                    .orElseThrow(() -> new GroupNotFoundException("Target group not found"));

            // Check if both groups are active
            boolean isFromGroupActive = groupActivityService.checkAndUpdateGroupActivity(fromGroup);
            boolean isToGroupActive = groupActivityService.checkAndUpdateGroupActivity(toGroup);

            if (!isFromGroupActive) {
                throw new GroupInactiveException("Cannot move students from inactive group: " + fromGroup.getName());
            }

            if (!isToGroupActive) {
                throw new GroupInactiveException("Cannot move students to inactive group: " + toGroup.getName());
            }

            if (!fromGroup.getTeacher().equals(teacher) || !toGroup.getTeacher().equals(teacher)) {
                throw new GroupMismatchException("You can only move students within your own groups");
            }

            Optional<Subject> fromSubject = subjectRepository.findByGroupsContaining(fromGroup);
            Optional<Subject> toSubject = subjectRepository.findByGroupsContaining(toGroup);

            if (fromSubject.isEmpty() || toSubject.isEmpty() || !fromSubject.get().equals(toSubject.get())) {
                throw new GroupMismatchException("Groups must belong to the same subject");
            }

            User student = userRepository.findByUsername(studentUsername)
                    .orElseThrow(() -> new UserNotFoundException("Student not found"));

            if (!fromGroup.getStudents().contains(student)) {
                throw new StudentNotInGroupException("Student is not in the source group");
            }

            List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student)
                    .stream()
                    .filter(g -> fromSubject.get().getGroups().contains(g))
                    .toList();

            if (studentActiveGroups.size() > 1 &&
                    studentActiveGroups.stream().anyMatch(g -> g.getId() != fromGroupId)) {
                throw new MultipleActiveGroupsException("Student is already in multiple active groups of this subject");
            }

            fromGroup.getStudents().remove(student);
            toGroup.getStudents().add(student);

            groupRepository.save(fromGroup);
            groupRepository.save(toGroup);

        } catch (UserNotFoundException | GroupNotFoundException | GroupInactiveException | GroupMismatchException |
                 StudentNotInGroupException | MultipleActiveGroupsException e) {
            log.error("Error moving student between groups: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while moving student between groups", e);
            throw new RuntimeException("Unexpected error occurred while moving student between groups", e);
        }
    }
}