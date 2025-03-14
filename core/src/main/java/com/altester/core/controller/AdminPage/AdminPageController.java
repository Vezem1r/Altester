package com.altester.core.controller.AdminPage;

import com.altester.core.dtos.AdminPage.AdminPageDTO;
import com.altester.core.dtos.AdminPage.UpdateUser;
import com.altester.core.dtos.AdminPage.UsersListDTO;
import com.altester.core.service.AdminPage.AdminPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminPageService adminPageService;

    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUser(@RequestBody UpdateUser updateUser, @RequestParam String username) {
        try {
            log.debug("Updating user with username: {} and new data: {}", username, updateUser);
            UsersListDTO updatedUser = adminPageService.updateUser(updateUser, username);
            log.info("User with username {} successfully updated to {}", username, updatedUser);
            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            String detailedErrorMessage = String.format("Error updating user %s with new username %s: %s",
                    username, updateUser.getUsername(), e.getMessage());
            log.error(detailedErrorMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/promoteTeacher")
    public ResponseEntity<String> promoteTeacher(@RequestParam String username) {
        try {
            log.debug("Promoting user {} to TEACHER role", username);
            adminPageService.promoteTeacher(username);
            log.info("User {} successfully promoted to TEACHER role", username);
            return ResponseEntity.ok("User successfully promoted to TEACHER");
        } catch (Exception e) {
            String detailedError = String.format("Error promoting user %s to TEACHER role: %s", username, e.getMessage());
            log.error(detailedError);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/promoteStudent")
    public ResponseEntity<String> promoteStudent(@RequestParam String username) {
        try {
            log.debug("Demoting user {} to STUDENT role", username);
            adminPageService.promoteStudent(username);
            log.info("User {} successfully demoted to STUDENT role", username);
            return ResponseEntity.ok("User successfully promoted to STUDENT");
        } catch (Exception e) {
            String detailedError = String.format("Error demoting user %s to STUDENT role: %s", username, e.getMessage());
            log.error(detailedError);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping()
    public ResponseEntity<AdminPageDTO> getAdminPage() {
        try {
            log.debug("Fetching admin page statistics");
            AdminPageDTO stats = adminPageService.getPage();
            log.debug("Admin stats fetched successfully: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching admin page statistics: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getStudents")
    public ResponseEntity<Page<UsersListDTO>> getStudents(@RequestParam(defaultValue = "0") int page) {
        try {
            log.debug("Fetching students list for page {}", page);
            Page<UsersListDTO> students = adminPageService.getStudents(page);
            log.debug("Successfully fetched {} students from page {}/{}",
                    students.getNumberOfElements(), page, students.getTotalPages() - 1);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students list for page {}: {}", page, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getTeachers")
    public ResponseEntity<Page<UsersListDTO>> getTeachers(@RequestParam(defaultValue = "0") int page) {
        try {
            log.debug("Fetching teachers list for page {}", page);
            Page<UsersListDTO> teachers = adminPageService.getTeachers(page);
            log.debug("Successfully fetched {} teachers from page {}/{}",
                    teachers.getNumberOfElements(), page, teachers.getTotalPages() - 1);
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error fetching teachers list for page {}: {}", page, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}