package com.altester.core.controller.page;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.service.AdminPageService;
import com.altester.core.util.CacheablePage;
import jakarta.validation.constraints.Pattern;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminPageController {

  private final AdminPageService adminPageService;

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
      @RequestParam(defaultValue = "all")
          @Pattern(
              regexp = "^(all|name|firstName|lastName|email|username)$",
              message =
                  "searchField must be one of: all, name, firstName, lastName, email, username")
          String searchField,
      @RequestParam(defaultValue = "all")
          @Pattern(
              regexp = "^(all|ldap|registered)$",
              message = "registrationFilter must be one of: all, ldap, registered")
          String registrationFilter) {
    CacheablePage<UsersListDTO> students =
        adminPageService.getStudents(page, searchQuery, searchField, registrationFilter);
    return ResponseEntity.ok(students);
  }

  @GetMapping("/getTeachers")
  public ResponseEntity<Page<UsersListDTO>> getTeachers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String searchQuery,
      @RequestParam(defaultValue = "all")
          @Pattern(
              regexp = "^(all|name|firstName|lastName|email|username)$",
              message =
                  "searchField must be one of: all, name, firstName, lastName, email, username")
          String searchField,
      @RequestParam(defaultValue = "all")
          @Pattern(
              regexp = "^(all|ldap|registered)$",
              message = "registrationFilter must be one of: all, ldap, registered")
          String registrationFilter) {
    CacheablePage<UsersListDTO> teachers =
        adminPageService.getTeachers(page, searchQuery, searchField, registrationFilter);
    return ResponseEntity.ok(teachers);
  }
}
