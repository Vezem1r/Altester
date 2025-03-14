package com.altester.core.controller.AdminPage;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import com.altester.core.service.subject.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/subject")
@Slf4j
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/create")
    public ResponseEntity<String> createSubject(@RequestBody CreateSubjectDTO createSubjectDTO) {
        try {
            subjectService.createSubject(createSubjectDTO);
            log.info("Subject created successfully {}", createSubjectDTO.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("Subject has been created successfully");
        } catch (Exception e) {
            log.error("Error creating subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create subject: " + e.getMessage());
        }
    }

    @PostMapping("/update-groups")
    public ResponseEntity<String> assignGroups (@RequestBody UpdateGroupsDTO updateGroupsDTO) {
        try {
            subjectService.updateGroups(updateGroupsDTO);
            log.info("Subject updated successfully {}", updateGroupsDTO.getSubjectId());
            return ResponseEntity.status(HttpStatus.OK).body("Subject has been updated successfully");
        } catch (Exception e) {
            log.error("Error updating groups: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update groups in subject: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{subjectId}")
    public ResponseEntity<String> deleteSubject(@PathVariable long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            log.info("Subject deleted successfully with id {}", subjectId);
            return ResponseEntity.ok("Subject has been deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error deleting subject: " + e.getMessage());
        }
    }

    @PutMapping("/update/{subjectId}")
    public ResponseEntity<String> updateSubject(@RequestBody CreateSubjectDTO createSubjectDTO, @PathVariable long subjectId) {
        try {
            subjectService.updateSubject(createSubjectDTO, subjectId);
            log.info("Subject updated successfully {}", createSubjectDTO.getName());
            return ResponseEntity.ok("Subject has been updated successfully");
        } catch (Exception e) {
            log.error("Error updating subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update subject: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<SubjectDTO>> getAllSubjects(@RequestParam(defaultValue = "0") int page) {
        try {
            int fixedSize = 10;
            Pageable pageable = PageRequest.of(page, fixedSize);
            Page<SubjectDTO> subjects = subjectService.getAllSubjects(pageable);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
