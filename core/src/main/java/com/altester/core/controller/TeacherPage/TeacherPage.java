package com.altester.core.controller.TeacherPage;

import com.altester.core.dtos.TeacherPage.ListTeacherGroupDTO;
import com.altester.core.dtos.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.TeacherPage.TeacherStudentsDTO;
import com.altester.core.service.TeacherPage.TeacherPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/getStudents")
    public ResponseEntity<Page<TeacherStudentsDTO>> getTeacherStudents(Pageable pageable, Principal principal) {
        try {
            String username = principal.getName();
            log.debug("Fetching students for teacher: {}", username);
            Page<TeacherStudentsDTO> students = teacherPageService.getStudents(username, pageable);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getGroups")
    public ResponseEntity<Page<ListTeacherGroupDTO>> getTeacherGroups(Pageable pageable, Principal principal) {
        try {
            String username = principal.getName();
            log.debug("Fetching groups for teacher: {}", username);
            Page<ListTeacherGroupDTO> groups = teacherPageService.getGroups(username, pageable);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("Error fetching groups: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/moveStudent")
    public ResponseEntity<String> moveStudentToAnotherGroup(
            @RequestParam String studentUsername,
            @RequestParam Long fromGroupId,
            @RequestParam Long toGroupId,
            Principal principal) {
        try {
            String username = principal.getName();
            log.debug("Moving student {} from group {} to group {} by teacher {}", studentUsername, fromGroupId, toGroupId, username);
            teacherPageService.moveStudentBetweenGroups(username, studentUsername, fromGroupId, toGroupId);
            return ResponseEntity.ok("Student moved successfully");
        } catch (Exception e) {
            log.error("Error moving student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error moving student");
        }
    }
}
