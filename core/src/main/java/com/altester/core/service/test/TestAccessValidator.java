package com.altester.core.service.test;

import com.altester.core.exception.GroupAccessDeniedException;
import com.altester.core.exception.TeacherEditNotAllowedException;
import com.altester.core.exception.TeacherTestCreatorException;
import com.altester.core.exception.TestAccessDeniedException;
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

    public void validateTestAccess(User currentUser, Test test) {
        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            boolean isTeacherTest = isTeacherTestCreator(currentUser, test, teacherGroups);

            if (!isTeacherTest) {
                throw new TestAccessDeniedException("Not authorized to access this test");
            }
        } else {
            throw new TestAccessDeniedException("Not authorized to access this test");
        }
    }

    public void validateGroupAccess(User currentUser, Group group) {
        if (currentUser.getRole() == RolesEnum.ADMIN) {
            return;
        }

        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            if (!teacherGroups.contains(group)) {
                throw new GroupAccessDeniedException("Not authorized to access this group");
            }
        } else {
            throw new GroupAccessDeniedException("Not authorized to access this group");
        }
    }

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
            throw new TeacherEditNotAllowedException("Teacher is not associated with this test");
        }

        if (test.isCreatedByAdmin()) {
            return test.isAllowTeacherEdit();
        }

        return true;
    }

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

        throw new TeacherTestCreatorException("Teacher is not the creator of this test");
    }
}