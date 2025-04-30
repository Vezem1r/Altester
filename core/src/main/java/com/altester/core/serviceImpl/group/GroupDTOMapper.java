package com.altester.core.serviceImpl.group;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GroupDTOMapper {

    public GroupDTO toGroupDTO(Group group, String subjectName, boolean isInFuture) {
        List<GroupUserList> students = mapUsersToGroupUserList(group.getStudents());

        GroupUserList teacher = toGroupUserList(group.getTeacher());

        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .subject(subjectName)
                .students(students)
                .teacher(teacher)
                .semester(group.getSemester())
                .academicYear(group.getAcademicYear())
                .active(group.isActive())
                .isInFuture(isInFuture)
                .build();
    }

    public GroupsResponse toGroupsResponse(Group group, String subjectName, boolean isInFuture) {
        GroupsResponse response = new GroupsResponse(
                group.getId(),
                group.getName(),
                group.getTeacher() != null ? group.getTeacher().getUsername() : "No teacher",
                group.getStudents().size(),
                subjectName,
                group.getSemester(),
                group.getAcademicYear(),
                group.isActive()
        );

        response.setInFuture(isInFuture);
        return response;
    }

    public GroupUserList toGroupUserList(User user) {
        if (user == null) {
            return null;
        }

        return new GroupUserList(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername()
        );
    }

    public List<GroupUserList> mapUsersToGroupUserList(Set<User> users) {
        return users.stream()
                .map(this::toGroupUserList)
                .toList();
    }

    public CreateGroupUserListDTO toCreateGroupUserListDTO(User user) {
        return new CreateGroupUserListDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername()
        );
    }

    public void enrichWithSubjectInfo(
            CreateGroupUserListDTO dto,
            boolean inSameSubject,
            String subjectName,
            String subjectShortName) {
    }

    public List<CreateGroupUserListDTO> mapAndSortCurrentMembers(Set<User> students) {
        return students.stream()
                .map(this::toCreateGroupUserListDTO)
                .sorted(Comparator.comparing(dto -> dto.getName() + " " + dto.getSurname()))
                .collect(Collectors.toList());
    }
}