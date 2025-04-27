package com.altester.core.serviceImpl.apiKey;

import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.TestGroupAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestGroupAssignmentManager {

    private final TestGroupAssignmentRepository assignmentRepository;
    private final GroupRepository groupRepository;

    /**
     * Assigns an API key to a test for a specific group.
     *
     * @param test the test
     * @param group the group
     * @param apiKey the API key to assign
     * @param currentUser the user making the assignment
     */
    public void assignApiKeyToTestAndGroup(Test test, Group group, ApiKey apiKey, User currentUser) {
        TestGroupAssignment assignment = assignmentRepository
                .findByTestAndGroup(test, group)
                .orElse(TestGroupAssignment.builder()
                        .test(test)
                        .group(group)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(currentUser)
                        .build());

        assignment.setApiKey(apiKey);
        assignmentRepository.save(assignment);
    }

    /**
     * Unassigns an API key from a test-group combination.
     *
     * @param test the test
     * @param group the group
     * @throws ResourceNotFoundException if the assignment doesn't exist
     * @throws StateConflictException if no API key is assigned
     */
    public void unassignApiKeyFromTestAndGroup(Test test, Group group) {
        TestGroupAssignment assignment = assignmentRepository
                .findByTestAndGroup(test, group)
                .orElseThrow(() -> new ResourceNotFoundException("assignment", "test and group",
                        test.getId() + " and " + group.getId()));

        if (assignment.getApiKey() == null) {
            throw new StateConflictException("assignment", "no_api_key",
                    "This test does not have an API key assigned for the specified group");
        }

        assignment.setApiKey(null);
        assignment.setAiEvaluation(false);
        assignmentRepository.save(assignment);
    }

    /**
     * Gets all groups for a test where the specified user is the teacher.
     *
     * @param user the user
     * @param test the test
     * @return a list of groups
     * @throws ValidationException if the user is an admin
     */
    public List<Group> getTeacherGroupsForTest(User user, Test test) {
        if (RolesEnum.ADMIN.equals(user.getRole())) {
            throw ValidationException.invalidParameter("groupId", "Admins must specify a group ID");
        }

        return groupRepository.findByTeacherAndTestsContaining(user, test);
    }

    /**
     * Handles the case when an API key is disabled by unassigning it from all test-group combinations.
     *
     * @param apiKey the disabled API key
     */
    public void handleDisabledApiKey(ApiKey apiKey) {
        List<TestGroupAssignment> assignments = assignmentRepository.findByApiKey(apiKey);
        for (TestGroupAssignment assignment : assignments) {
            assignment.setAiEvaluation(false);
            assignment.setApiKey(null);
        }
        assignmentRepository.saveAll(assignments);

        if (!assignments.isEmpty()) {
            log.info("Disabled AI evaluation and unassigned API key {} from {} test-group combinations",
                    apiKey.getId(), assignments.size());
        }
    }
}