package com.altester.core.serviceImpl.test;

import com.altester.core.exception.AccessDeniedException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TestAccessValidator {
    private final GroupRepository groupRepository;
    private final TestDTOMapper testDTOMapper;

    /**
     * Validates if the current user has permission to access the specified test
     *
     * @param currentUser The user attempting to access the test
     * @param test The test being accessed
     * @throws AccessDeniedException if the user does not have appropriate permissions
     */
    public void validateTestAccess(User currentUser, Test test) {
        log.debug("Validating test access for user: {}, test ID: {}", currentUser.getUsername(), test.getId());

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            boolean isTeacherTest = hasTestGroupAssociation(currentUser, test, teacherGroups);

            if (!isTeacherTest) {
                log.warn("Access denied: Teacher {} attempted to access unauthorized test ID: {}",
                        currentUser.getUsername(), test.getId());
                throw AccessDeniedException.testAccess();
            }
        } else {
            log.warn("Access denied: User {} with role {} attempted to access test ID: {}",
                    currentUser.getUsername(), currentUser.getRole(), test.getId());
            throw AccessDeniedException.testAccess();
        }
    }

    /**
     * Validates if the current user has permission to access the specified group
     *
     * @param currentUser The user attempting to access the group
     * @param group The group being accessed
     * @throws AccessDeniedException if the user does not have appropriate permissions
     */
    public void validateGroupAccess(User currentUser, Group group) {
        log.debug("Validating group access for user: {}, group ID: {}", currentUser.getUsername(), group.getId());

        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            if (!teacherGroups.contains(group)) {
                log.warn("Access denied: Teacher {} attempted to access unauthorized group: {}",
                        currentUser.getUsername(), group.getName());
                throw AccessDeniedException.groupAccess();
            }
        } else {
            log.warn("Access denied: User {} with role {} attempted to access group: {}",
                    currentUser.getUsername(), currentUser.getRole(), group.getName());
            throw AccessDeniedException.groupAccess();
        }
    }

    /**
     * Checks if a teacher can edit a specific test based on permission rules:
     * - Teachers can edit tests they created for their groups
     * - Teachers can only edit admin-created tests if allowTeacherEdit flag is true
     * - Teachers must be associated with at least one group assigned to the test
     *
     * @param teacher The teacher user requesting edit access
     * @param test The test to be edited
     * @param teacherGroups Optional list of groups the teacher is associated with (will be loaded if null)
     * @throws AccessDeniedException if the teacher cannot edit the test
     */
    public void validateTeacherEditAccess(User teacher, Test test, List<Group> teacherGroups) {
        log.debug("Validating teacher edit access for user: {}, test ID: {}", teacher.getUsername(), test.getId());

        if (teacher.getRole() != RolesEnum.TEACHER) {
            log.warn("Non-teacher user {} attempted to use teacher edit validation", teacher.getUsername());
            throw AccessDeniedException.roleConflict();
        }

        if (teacherGroups == null) {
            teacherGroups = groupRepository.findByTeacher(teacher);
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);

        boolean isTeacherAssociated = hasGroupIntersection(teacherGroups, testGroups);

        if (!isTeacherAssociated) {
            log.warn("Edit access denied: Teacher {} is not associated with test ID: {}",
                    teacher.getUsername(), test.getId());
            throw AccessDeniedException.testEdit();
        }

        if (test.isCreatedByAdmin() && !test.isAllowTeacherEdit()) {
            log.warn("Edit access denied: Teacher {} attempted to edit admin test ID: {} with teacher edit not allowed",
                    teacher.getUsername(), test.getId());
            throw AccessDeniedException.testEdit();
        }
    }

    /**
     * Verifies if a teacher has any association with a test through their assigned groups
     *
     * @param teacher The teacher user to check
     * @param test The test to verify association with
     * @param teacherGroups Optional list of groups the teacher is associated with (will be loaded if null)
     * @return boolean indicating whether the teacher has any association with the test
     * @throws AccessDeniedException if the teacher has no groups associated with the test
     */
    public boolean hasTestGroupAssociation(User teacher, Test test, List<Group> teacherGroups) {
        log.debug("Checking test-group association for teacher: {}, test ID: {}", teacher.getUsername(), test.getId());

        if (teacherGroups == null) {
            teacherGroups = groupRepository.findByTeacher(teacher);
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
        return hasGroupIntersection(teacherGroups, testGroups);
    }

    /**
     * Helper method to check if there is any intersection between two group lists
     *
     * @param groupList1 First list of groups
     * @param groupList2 Second list of groups
     * @return true if there is at least one group in common, false otherwise
     */
    private boolean hasGroupIntersection(List<Group> groupList1, List<Group> groupList2) {
        for (Group group : groupList2) {
            if (groupList1.contains(group)) {
                return true;
            }
        }
        return false;
    }
}