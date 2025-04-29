package com.altester.core.service;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.*;
import com.altester.core.util.CacheablePage;

public interface GroupService {

    /**
     * Deletes a group if it is active and can be modified.
     * Groups from past semesters (inactive) cannot be deleted.
     *
     * @param id ID of the group to delete
     * @throws ResourceNotFoundException if the group doesn't exist
     * @throws StateConflictException if the group is from a past semester and cannot be modified
     * @throws ValidationException if there's an error during deletion
     */
    void deleteGroup(long id);

    /**
     * Retrieves detailed information about a specific group, including subject association.
     *
     * @param id ID of the group to retrieve
     * @return GroupDTO containing group details and associated subject information
     * @throws ResourceNotFoundException if the group doesn't exist
     */
    GroupDTO getGroup(long id);

    /**
     * Returns a paginated list of groups with optional filtering
     * @param searchQuery Optional search query to filter groups by name, teacher, or semester
     * @param activityFilter Optional filter by activity status ("active", "inactive", "future")
     * @param available Filter for groups not associated with any subject
     * @param subjectId Filter for groups associated with a specific subject
     * @return Paginated list of GroupsResponse objects
     */
    CacheablePage<GroupsResponse> getAllGroups(int page, int size, String searchQuery, String activityFilter,
                                               Boolean available, Long subjectId);

    /**
     * Updates an existing group with new information if allowed by activity rules
     * @param id ID of the group to update
     * @param updateGroupDTO DTO containing updated group information
     * @throws StateConflictException if group cannot be modified due to activity constraints
     * @throws ValidationException if update fails validation
     * @throws ResourceAlreadyExistsException if new group name is already taken
     */
    void updateGroup(Long id, UpdateGroupDTO updateGroupDTO);

    /**
     * Creates a new group with provided information and returns the generated ID
     * @param createGroupDTO DTO containing new group information
     * @return ID of the newly created group
     * @throws ResourceAlreadyExistsException if group name already exists
     * @throws StateConflictException if teacher role validation fails
     */
    Long createGroup(CreateGroupDTO createGroupDTO);

    /**
     * Retrieves a paginated and searchable list of all students in the system.
     * Used for group creation/modification to select which students to add.
     *
     * @param page Page number (zero-based)
     * @param size Number of items per page
     * @param searchQuery Optional search text to filter students by name, email, or username
     * @return Paginated list of students available for assignment to groups
     */
    CacheablePage<CreateGroupUserListDTO> getAllStudents(int page, int size, String searchQuery);

    /**
     * Retrieves a paginated and searchable list of all teachers in the system.
     * Used for group creation/modification to select a teacher for the group.
     *
     * @param page Page number (zero-based)
     * @param size Number of items per page
     * @param searchQuery Optional search text to filter teachers by name, email, or username
     * @return Paginated list of teachers available for assignment to a group
     */
    CacheablePage<GroupUserList> getAllTeachers(int page, int size, String searchQuery);

    /**
     * Gets current group members and available students categorized for management screens
     * @param groupId ID of the group to get members for
     * @param searchQuery Optional search query to filter available students
     * @param includeCurrentMembers Whether to include current members in available students
     * @return GroupStudentsResponseDTO with current members and available students
     * @throws ValidationException if groupId is null
     */
    GroupStudentsResponseDTO getGroupStudentsWithCategories(
            int page, int size, Long groupId, String searchQuery, boolean includeCurrentMembers);

    /**
     * Retrieves students not in specified group with optional filtering by subject association
     * @param groupId ID of the group to exclude students from
     * @param searchQuery Optional search query to filter students
     * @return Paginated list of CreateGroupUserListDTO objects
     */
    CacheablePage<CreateGroupUserListDTO> getAllStudentsNotInGroup(
            int page, int size, Long groupId, String searchQuery);
}