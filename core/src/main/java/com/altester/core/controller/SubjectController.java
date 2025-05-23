package com.altester.core.controller;

import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.service.SubjectService;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
