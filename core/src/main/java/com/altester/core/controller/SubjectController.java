package com.altester.core.controller;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.service.SubjectService;
import com.altester.core.util.CacheablePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/subject")
@Slf4j
@RequiredArgsConstructor
@Validated
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping("/create")
  public ResponseEntity<String> createSubject(
      @Valid @RequestBody CreateSubjectDTO createSubjectDTO) {
    subjectService.createSubject(createSubjectDTO);
    log.info("Subject created successfully {}", createSubjectDTO.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body("Subject has been created successfully");
  }

  @DeleteMapping("/delete/{subjectId}")
  public ResponseEntity<String> deleteSubject(@PathVariable long subjectId) {
    subjectService.deleteSubject(subjectId);
    log.info("Subject deleted successfully with id {}", subjectId);
    return ResponseEntity.ok("Subject has been deleted successfully");
  }

  @PutMapping("/update/{subjectId}")
  public ResponseEntity<String> updateSubject(
      @Valid @RequestBody CreateSubjectDTO createSubjectDTO, @PathVariable long subjectId) {
    subjectService.updateSubject(createSubjectDTO, subjectId);
    log.info("Subject updated successfully {}", createSubjectDTO.getName());
    return ResponseEntity.ok("Subject has been updated successfully");
  }

  @GetMapping("/all")
  public ResponseEntity<Page<SubjectDTO>> getAllSubjects(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String searchQuery) {
    CacheablePage<SubjectDTO> subjects = subjectService.getAllSubjects(page, size, searchQuery);
    log.debug(
        "Retrieved {} subjects with search query: {}", subjects.getTotalElements(), searchQuery);
    return ResponseEntity.ok(subjects);
  }
}
