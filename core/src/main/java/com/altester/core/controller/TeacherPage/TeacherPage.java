package com.altester.core.controller.TeacherPage;

import com.altester.core.dtos.TeacherPage.TeacherPageDTO;
import com.altester.core.service.TeacherPage.TeacherPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/teacher")
@Slf4j
@RequiredArgsConstructor
public class TeacherPage {

    private final TeacherPageService teacherPageService;


    @GetMapping()
    ResponseEntity<TeacherPageDTO> getTeacherPage(Principal principal) {
        try {
            String username = principal.getName();
            log.debug("Fetching teacher page for {}", username);
            TeacherPageDTO teacherPageDTO = teacherPageService.getPage(username);
            log.debug("Successfully fetched teacher page for {}", username);
            return ResponseEntity.ok(teacherPageDTO);
        } catch (Exception e) {
            log.error("Error fetching teacher page. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
