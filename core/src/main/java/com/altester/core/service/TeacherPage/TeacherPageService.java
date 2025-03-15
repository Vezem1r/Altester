package com.altester.core.service.TeacherPage;

import com.altester.core.dtos.TeacherPage.TeacherGroupDTO;
import com.altester.core.dtos.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.TeacherPage.TeacherSubjectDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherPageService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;

    public TeacherPageDTO getPage(String username) {
            User teacher = userRepository.findByUsername(username).orElseThrow(()
                    -> new RuntimeException("Teacher not found" + username));

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
                                        group.getTests().size()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return new TeacherPageDTO(username, subjectDTOS);
    }
}
