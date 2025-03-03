package com.altester.core.controller.subject;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.model.auth.Subject;
import com.altester.core.service.subject.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return ResponseEntity.status(HttpStatus.CREATED).body("Subject has been created successfully");
        } catch (Exception e) {
            log.error("Error creating subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create subject: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{subjectId}")
    public ResponseEntity<String> deleteSubject(@PathVariable long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            return ResponseEntity.ok("Subject has been deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subject not found: " + e.getMessage());
        }
    }

    @PutMapping("/update/{subjectId}")
    public ResponseEntity<String> updateSubject(@RequestBody CreateSubjectDTO createSubjectDTO, @PathVariable long subjectId) {
        try {
            subjectService.updateSubject(createSubjectDTO, subjectId);
            return ResponseEntity.ok("Subject has been updated successfully");
        } catch (Exception e) {
            log.error("Error updating subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update subject: " + e.getMessage());
        }
    }

    @GetMapping("/get/{subjectId}")
    public ResponseEntity<Subject> getSubject(@PathVariable long subjectId) {
        try {
            Subject subject = subjectService.getSubject(subjectId);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            log.error("Subject not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            log.error("Error fetching subjects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
