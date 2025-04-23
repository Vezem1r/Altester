package com.altester.core.controller.page;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UpdateUser;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.service.AdminPageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminPageController {

    private final AdminPageService adminPageService;

    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUser(
            @Valid @RequestBody UpdateUser updateUser,
            @RequestParam String username) {
        log.debug("Updating user with username: {} and new data: {}", username, updateUser);
        UsersListDTO updatedUser = adminPageService.updateUser(updateUser, username);
        log.info("User with username {} successfully updated to {}", username, updatedUser);
        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping("/promoteTeacher")
    public ResponseEntity<String> promoteTeacher(@RequestParam String username) {
        log.debug("Promoting user {} to TEACHER role", username);
        adminPageService.promoteToTeacher(username);
        log.info("User {} successfully promoted to TEACHER role", username);
        return ResponseEntity.ok("User successfully promoted to TEACHER");
    }

    @PutMapping("/promoteStudent")
    public ResponseEntity<String> promoteStudent(@RequestParam String username) {
        log.debug("Demoting user {} to STUDENT role", username);
        adminPageService.demoteToStudent(username);
        log.info("User {} successfully demoted to STUDENT role", username);
        return ResponseEntity.ok("User successfully promoted to STUDENT");
    }

    @GetMapping()
    public ResponseEntity<AdminPageDTO> getAdminPage(Principal principal) {
        String username = principal.getName();
        log.debug("Fetching admin page statistics");
        AdminPageDTO stats = adminPageService.getPage(username);
        log.debug("Admin stats fetched successfully: {}", stats);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/getStudents")
    public ResponseEntity<Page<UsersListDTO>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "all") @Pattern(regexp = "^(all|name|firstName|lastName|email|username)$",
                    message = "searchField must be one of: all, name, firstName, lastName, email, username") String searchField,
            @RequestParam(defaultValue = "all") @Pattern(regexp = "^(all|ldap|registered)$",
                    message = "registrationFilter must be one of: all, ldap, registered") String registrationFilter) {
        log.debug("Fetching students list for page {} with search: {}, field: {}, registrationFilter: {}",
                page, searchQuery, searchField, registrationFilter);
        Page<UsersListDTO> students = adminPageService.getStudents(page, searchQuery, searchField, registrationFilter);
        log.debug("Successfully fetched {} students from page {}/{}",
                students.getNumberOfElements(), page, students.getTotalPages() - 1);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/getTeachers")
    public ResponseEntity<Page<UsersListDTO>> getTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "all") @Pattern(regexp = "^(all|name|firstName|lastName|email|username)$",
                    message = "searchField must be one of: all, name, firstName, lastName, email, username") String searchField,
            @RequestParam(defaultValue = "all") @Pattern(regexp = "^(all|ldap|registered)$",
                    message = "registrationFilter must be one of: all, ldap, registered") String registrationFilter) {
        log.debug("Fetching teachers list for page {} with search: {}, field: {}, registrationFilter: {}",
                page, searchQuery, searchField, registrationFilter);
        Page<UsersListDTO> teachers = adminPageService.getTeachers(page, searchQuery, searchField, registrationFilter);
        log.debug("Successfully fetched {} teachers from page {}/{}",
                teachers.getNumberOfElements(), page, teachers.getTotalPages() - 1);
        return ResponseEntity.ok(teachers);
    }
}