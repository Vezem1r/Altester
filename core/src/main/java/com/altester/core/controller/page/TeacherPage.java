package com.altester.core.controller.page;

import com.altester.core.dtos.core_service.TeacherPage.ListTeacherGroupDTO;
import com.altester.core.dtos.core_service.TeacherPage.MoveStudentRequest;
import com.altester.core.dtos.core_service.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherStudentsDTO;
import com.altester.core.service.TeacherPageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
@Slf4j
@RequiredArgsConstructor
@Validated
public class TeacherPage {

  private final TeacherPageService teacherPageService;

  @GetMapping()
  ResponseEntity<TeacherPageDTO> getTeacherPage(Principal principal) {
    log.debug("Fetching teacher page for {}", principal.getName());
    TeacherPageDTO teacherPageDTO = teacherPageService.getPage(principal);
    log.debug("Successfully fetched teacher page for {}", principal.getName());
    return ResponseEntity.ok(teacherPageDTO);
  }

  @GetMapping("/getStudents")
  public ResponseEntity<Page<TeacherStudentsDTO>> getTeacherStudents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      Principal principal) {

    log.debug("Fetching students: search: {}", search);
    Page<TeacherStudentsDTO> students =
        teacherPageService.getStudents(principal, page, size, search);
    return ResponseEntity.ok(students);
  }

  @GetMapping("/getGroups")
  public ResponseEntity<Page<ListTeacherGroupDTO>> getTeacherGroups(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false)
          @Pattern(
              regexp = "^(active|inactive|future)?$",
              message = "Status must be one of: active, inactive, future, or empty")
          String status,
      Principal principal) {

    log.debug("Fetching groups: search: {}, status: {}", search, status);

    Page<ListTeacherGroupDTO> groups =
        teacherPageService.getGroups(principal, page, size, search, status);

    return ResponseEntity.ok(groups);
  }

  @PostMapping("/moveStudent")
  public ResponseEntity<String> moveStudentToAnotherGroup(
      @Valid @RequestBody MoveStudentRequest request, Principal principal) {

    log.debug(
        "Moving student {} from group {} to group {}",
        request.getStudentUsername(),
        request.getFromGroupId(),
        request.getToGroupId());

    teacherPageService.moveStudentBetweenGroups(principal, request);

    return ResponseEntity.ok("Student moved successfully");
  }
}
