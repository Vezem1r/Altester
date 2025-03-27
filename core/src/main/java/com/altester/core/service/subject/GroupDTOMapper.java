package com.altester.core.service.subject;

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

    /**
     * Converts a Group entity to a complete GroupDTO with all associated data
     * @param group Group entity to convert
     * @param subjectName Name of the subject associated with the group
     * @param isInFuture Whether the group is in a future semester
     * @return Complete GroupDTO with all information
     */
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

    /**
     * Transforms a Group entity into a simplified GroupsResponse for list views
     * @param group Group entity to convert
     * @param subjectName Name of the subject associated with the group
     * @param isInFuture Whether the group is in a future semester
     * @return GroupsResponse with basic group information
     */
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

    /**
     * Maps a User entity to GroupUserList DTO with basic user information
     * @param user User entity to convert
     * @return GroupUserList DTO or null if user is null
     */
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

    /**
     * Converts a set of User entities to a list of GroupUserList DTOs
     * @param users Set of User entities to convert
     * @return List of GroupUserList DTOs
     */
    public List<GroupUserList> mapUsersToGroupUserList(Set<User> users) {
        return users.stream()
                .map(this::toGroupUserList)
                .toList();
    }

    /**
     * Creates a CreateGroupUserListDTO from a User entity with subject information
     * @param user User entity to convert
     * @param subjectNames List of subject names associated with the user
     * @return CreateGroupUserListDTO with user and subject information
     */
    public CreateGroupUserListDTO toCreateGroupUserListDTO(User user, List<String> subjectNames) {
        return new CreateGroupUserListDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                subjectNames
        );
    }

    /**
     * Enhances a CreateGroupUserListDTO with subject relationship details if applicable
     * @param dto DTO to enhance
     * @param inSameSubject Whether the user belongs to the same subject
     * @param subjectName Full subject name
     * @param subjectShortName Short name of the subject
     */
    public void enrichWithSubjectInfo(
            CreateGroupUserListDTO dto,
            boolean inSameSubject,
            String subjectName,
            String subjectShortName) {

        if (inSameSubject) {
            dto.setInSameSubject(true);
            dto.setSubjectName(subjectName);
            dto.setSubjectShortName(subjectShortName);
        }
    }

    /**
     * Maps and sorts group members to DTOs in alphabetical order by name
     * @param students Set of User entities representing students
     * @param subjectNamesList List of subject names for contextual information
     * @return Sorted list of CreateGroupUserListDTO objects
     */
    public List<CreateGroupUserListDTO> mapAndSortCurrentMembers(Set<User> students, List<String> subjectNamesList) {

        return students.stream()
                .map(student -> toCreateGroupUserListDTO(
                        student,
                        subjectNamesList
                )).sorted(Comparator.comparing(dto -> dto.getName() + " " + dto.getSurname())).collect(Collectors.toList());
    }
}