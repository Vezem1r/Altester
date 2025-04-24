package com.altester.core.serviceImpl.studentPage;

import com.altester.core.exception.AccessDeniedException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAccessValidator {
    private final GroupRepository groupRepository;

    public void ensureStudentRole(User user) {
        if (user.getRole() != RolesEnum.STUDENT) {
            throw AccessDeniedException.roleConflict();
        }
    }

    /**
     * Validates that the attempt belongs to the student
     */
    public void validateAttemptOwnership(Attempt attempt, User student) {
        if (!Objects.equals(attempt.getStudent().getId(), student.getId())) {
            throw AccessDeniedException.testAccess();
        }
    }

    /**
     * Validates that the student has access to the test
     */
    public void validateStudentTestAccess(User student, Test test) {
        List<Group> studentGroups = groupRepository.findAllByStudentId(student.getId());

        boolean isTestInStudentGroup = studentGroups.stream()
                .anyMatch(group -> group.getTests().stream()
                        .anyMatch(t -> t.getId() == test.getId()));

        if (!isTestInStudentGroup) {
            throw AccessDeniedException.testAccess();
        }
    }
}