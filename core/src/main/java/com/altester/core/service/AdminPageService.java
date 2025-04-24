package com.altester.core.service;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UpdateUser;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.util.CacheablePage;
import org.springframework.data.domain.Page;

public interface AdminPageService {

    /**
     * @param searchQuery Optional search text to filter students
     * @param searchField Field to search in ("all", "name", "firstName", "lastName", "email", "username")
     * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
     * @return Paginated list of UsersListDTO objects
     */
    CacheablePage<UsersListDTO> getStudents(int page, String searchQuery, String searchField, String registrationFilter);

    /**
     * @param searchQuery Optional search text to filter teachers
     * @param searchField Field to search in ("all", "name", "firstName", "lastName", "email", "username")
     * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
     * @return Paginated list of UsersListDTO objects
     */
    CacheablePage<UsersListDTO> getTeachers(int page, String searchQuery, String searchField, String registrationFilter);

    /**
     * Retrieves admin page data with system statistics
     * @param username Username of the admin user
     * @return AdminPageDTO with system statistics
     * @throws ResourceNotFoundException if user with given username doesn't exist
     */
    AdminPageDTO getPage(String username);

    void demoteToStudent(String username);

    void promoteToTeacher(String username);

    UsersListDTO updateUser(UpdateUser updateUser, String username);
}