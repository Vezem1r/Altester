package com.altester.core.service.test;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestDTOMapper {
    private final GroupRepository groupRepository;

    public TestSummaryDTO convertToTestSummaryDTO(Test test) {
        return TestSummaryDTO.builder()
                .id(test.getId())
                .title(test.getTitle())
                .duration(test.getDuration())
                .isOpen(test.isOpen())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .totalScore(test.getTotalScore())
                .maxAttempts(test.getMaxAttempts())
                .build();
    }

    public TestPreviewDTO convertToTestPreviewDTO(Test test, User currentUser) {
        List<Group> testGroups = findGroupsByTest(test);
        List<GroupSummaryDTO> associatedGroups = new ArrayList<>();

        List<Group> teacherGroups = currentUser.getRole() == RolesEnum.TEACHER
                ? groupRepository.findByTeacher(currentUser)
                : Collections.emptyList();

        for (Group group : testGroups) {
            if (currentUser.getRole() == RolesEnum.ADMIN ||
                    (currentUser.getRole() == RolesEnum.TEACHER && teacherGroups.contains(group))) {
                associatedGroups.add(GroupSummaryDTO.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .build());
            }
        }

        List<QuestionDTO> questions = test.getQuestions().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());

        TestPreviewDTO.TestPreviewDTOBuilder builder = TestPreviewDTO.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .duration(test.getDuration())
                .isOpen(test.isOpen())
                .maxAttempts(test.getMaxAttempts())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .isCreatedByAdmin(test.isCreatedByAdmin())
                .totalQuestions(test.getQuestions().size())
                .totalScore(test.getTotalScore())
                .associatedGroups(associatedGroups)
                .questions(questions);

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            builder.allowTeacherEdit(test.isAllowTeacherEdit());
        }

        return builder.build();
    }

    public QuestionDTO convertToQuestionDTO(Question question) {
        List<OptionDTO> options = question.getOptions() != null
                ? question.getOptions().stream()
                .map(this::convertToOptionDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .imagePath(question.getImagePath())
                .score(question.getScore())
                .questionType(question.getQuestionType())
                .options(options)
                .build();
    }

    public OptionDTO convertToOptionDTO(Option option) {
        return OptionDTO.builder()
                .id(option.getId())
                .text(option.getText())
                .description(option.getDescription())
                .isCorrect(option.isCorrect())
                .build();
    }

    public List<Group> findGroupsByTest(Test test) {
        List<Group> allGroups = groupRepository.findAll();
        return allGroups.stream()
                .filter(group -> group.getTests().contains(test))
                .collect(Collectors.toList());
    }
}