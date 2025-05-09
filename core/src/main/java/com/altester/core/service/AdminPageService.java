package com.altester.core.service;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UpdateUser;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceAlreadyExistsException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.util.CacheablePage;

public interface AdminPageService {

  /**
   * @param searchQuery Optional search text to filter students
   * @param searchField Field to search in ("all", "firstName", "lastName", "email", "username")
   * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
   * @return Paginated list of UsersListDTO objects
   */
  CacheablePage<UsersListDTO> getStudents(
      int page, String searchQuery, String searchField, String registrationFilter);

  /**
   * @param searchQuery Optional search text to filter teachers
   * @param searchField Field to search in ("all", "name", "firstName", "lastName", "email",
   *     "username")
   * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
   * @return Paginated list of UsersListDTO objects
   */
  CacheablePage<UsersListDTO> getTeachers(
      int page, String searchQuery, String searchField, String registrationFilter);

  /**
   * Retrieves admin page data with system statistics
   *
   * @param username Username of the admin user
   * @return AdminPageDTO with system statistics
   * @throws ResourceNotFoundException if user with given username doesn't exist
   */
  AdminPageDTO getPage(String username);

  /**
   * Demotes a user from a TEACHER role to a STUDENT role.\ Can't apply on users created with LDAP
   *
   * @param username The username of the user to be demoted
   * @throws ResourceNotFoundException if user with given username doesn't exist
   * @throws StateConflictException if the user is already a student
   */
  void demoteToStudent(String username);

  /**
   * Promotes a user to a TEACHER role. Can't apply on users created with LDAP
   *
   * @param username The username of the user to be promoted
   * @throws ResourceNotFoundException if user with given username doesn't exist
   * @throws StateConflictException if the user is already a teacher
   */
  void promoteToTeacher(String username);

  /**
   * Updates a user's personal information. Can't apply on users created with LDAP
   *
   * @param updateUser DTO containing updated user information (name, lastname, email, username)
   * @param username The current username of the user to be updated
   * @return Updated user information as UsersListDTO
   * @throws ResourceNotFoundException if user with given username doesn't exist
   * @throws AccessDeniedException if attempting to update an LDAP-created user
   * @throws ResourceAlreadyExistsException if the new username already exists in the system
   */
  UsersListDTO updateUser(UpdateUser updateUser, String username);
}
