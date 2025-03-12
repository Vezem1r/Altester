package com.altester.core.controller.AdminPage;

import com.altester.core.dtos.AdminPage.AdminPageDTO;
import com.altester.core.dtos.AdminPage.UsersListDTO;
import com.altester.core.dtos.auth_service.auth.UserDTO;
import com.altester.core.service.AdminPage.AdminPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminPageService adminPageService;

    @GetMapping()
    public ResponseEntity<AdminPageDTO> getAdminPage() {
        try {
            return ResponseEntity.ok(adminPageService.getPage());
        } catch (Exception e) {
            log.error("Error getting admin page. {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getStudents")
    public ResponseEntity<Page<UsersListDTO>> getStudents(@RequestParam(defaultValue = "0") int page) {
        try {
            return ResponseEntity.ok(adminPageService.getStudents(page));
        } catch (Exception e) {
            log.error("Error in getStudents: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getTeachers")
    public ResponseEntity<Page<UsersListDTO>> getTeachers(@RequestParam(defaultValue = "0") int page) {
        try {
            return ResponseEntity.ok(adminPageService.getTeachers(page));
        } catch (Exception e) {
            log.error("Error in getTeachers: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
