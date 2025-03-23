package com.altester.core.service.test;

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
     * Validate if a user has access to a test
     * @throws RuntimeException if access is denied
     */
    public void validateTestAccess(User currentUser, Test test) {
        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            boolean isTeacherTest = isTeacherTestCreator(currentUser, test, teacherGroups);

            if (!isTeacherTest) {
                throw new RuntimeException("Not authorized to access this test");
            }
        } else {
            throw new RuntimeException("Not authorized to access this test");
        }
    }

    /**
     * Validate if a user has access to a group
     * @throws RuntimeException if access is denied
     */
    public void validateGroupAccess(User currentUser, Group group) {
        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            if (!teacherGroups.contains(group)) {
                throw new RuntimeException("Not authorized to access this group");
            }
        } else {
            throw new RuntimeException("Not authorized to access this group");
        }
    }

    /**
     * Check if a teacher can edit a test.
     * Teachers can edit a test if:
     * 1. It's their own test, OR
     * 2. It's an admin-created test with allowTeacherEdit=true, and they have access to it
     * @return true if the teacher can edit the test
     */
    public boolean canTeacherEditTest(User teacher, Test test, List<Group> teacherGroups) {
        if (teacherGroups == null) {
            teacherGroups = groupRepository.findByTeacher(teacher);
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);

        boolean isTeacherAssociated = false;
        for (Group group : testGroups) {
            if (teacherGroups.contains(group)) {
                isTeacherAssociated = true;
                break;
            }
        }

        if (!isTeacherAssociated) {
            return false;
        }

        if (test.isCreatedByAdmin()) {
            return test.isAllowTeacherEdit();
        }

        return true;
    }

    /**
     * Check if a teacher is associated with a test through any of their groups
     * @return true if the teacher is associated with the test
     */
    public boolean isTeacherTestCreator(User teacher, Test test, List<Group> teacherGroups) {
        if (teacherGroups == null) {
            teacherGroups = groupRepository.findByTeacher(teacher);
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);

        for (Group group : testGroups) {
            if (teacherGroups.contains(group)) {
                return true;
            }
        }

        return false;
    }
}