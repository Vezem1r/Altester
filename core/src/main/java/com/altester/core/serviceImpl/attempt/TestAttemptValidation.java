package com.altester.core.serviceImpl.attempt;

import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.GroupRepository;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TestAttemptValidation {

    private final GroupRepository groupRepository;
    private final AttemptRepository attemptRepository;
    private final GroupActivityService groupActivityService;

    public void ensureStudentRole(User user) {
        if (user.getRole() != RolesEnum.STUDENT) {
            throw AccessDeniedException.roleConflict();
        }
    }

    public void validateStudentTestAccess(User student, Test test) {
        List<Group> studentGroups = groupRepository.findAllByStudentId(student.getId());

        boolean isTestInStudentGroup = studentGroups.stream()
                .filter(Group::isActive)
                .filter(group -> !groupActivityService.isGroupInFuture(group))
                .anyMatch(group -> group.getTests().stream()
                        .anyMatch(t -> t.getId() == test.getId()));

        if (!isTestInStudentGroup) {
            throw AccessDeniedException.testAccess();
        }
    }

    public void validateAttemptOwnership(Attempt attempt, User student) {
        if (!Objects.equals(attempt.getStudent().getId(), student.getId())) {
            throw AccessDeniedException.testAccess();
        }
    }

    public void validateTestAvailability(Test test, User student) {
        if (!test.isOpen()) {
            throw new StateConflictException("test", "closed", "Test is not open for attempts");
        }

        LocalDateTime now = LocalDateTime.now();

        if (test.getStartTime() != null && now.isBefore(test.getStartTime())) {
            throw new StateConflictException("test", "not_started",
                    "Test is not yet available. It starts at " + test.getStartTime());
        }

        if (test.getEndTime() != null && now.isAfter(test.getEndTime())) {
            throw new StateConflictException("test", "ended",
                    "Test has ended. The end time was " + test.getEndTime());
        }

        List<Attempt> studentAttempts = attemptRepository.findByTestAndStudent(test, student);

        long completedAttempts = studentAttempts.stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED)
                .count();

        Integer maxAttempts = test.getMaxAttempts();

        if (maxAttempts != null && completedAttempts >= maxAttempts) {
            throw new StateConflictException("test", "max_attempts_reached",
                    "Maximum number of attempts reached for this test");
        }

        if (test.getQuestions().isEmpty()) {
            throw new StateConflictException("test", "no_questions", "Test has no questions");
        }
    }

    public boolean isAttemptExpired(Attempt attempt) {
        LocalDateTime expirationTime = attempt.getStartTime().plusMinutes(attempt.getTest().getDuration());
        return LocalDateTime.now().isAfter(expirationTime);
    }
}