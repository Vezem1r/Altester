package com.altester.core.controller.TeacherPage;

import com.altester.core.dtos.core_service.subject.CreateTestDTO;
import com.altester.core.dtos.core_service.subject.TestsListDTO;
import com.altester.core.service.subject.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/teacher/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/create")
    public ResponseEntity<String> createTest(@Valid @RequestBody CreateTestDTO createTestDTO,
                                             Principal principal) {
        try {
            testService.createTest(createTestDTO, principal);
            return ResponseEntity.status(HttpStatus.CREATED).body("Test has been created successfully");
        } catch (Exception e) {
            log.error("Error creating test.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating test. " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateTest(@RequestBody CreateTestDTO createTestDTO,
                                             Principal principal,
                                             @PathVariable long id) {
        try {
            testService.updateTest(createTestDTO, principal, id);
            return ResponseEntity.status(HttpStatus.OK).body("Test has been updates successfully");
        } catch (Exception e) {
            log.error("Error updating test.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating test. " + e.getMessage());
        }
    }

    @GetMapping("/getAllByGroup/{group_id}")
    public ResponseEntity<Page<TestsListDTO>> getAllTests(Principal principal, @RequestParam(defaultValue = "0") int page, @PathVariable long group_id) {
        try {
            int fixedSize = 10;
            Pageable pageable = PageRequest.of(page, fixedSize);
            Page<TestsListDTO> tests = testService.getAllTestsByGroup(principal, pageable, group_id);
            return ResponseEntity.status(HttpStatus.CREATED).body(tests);
        } catch (Exception e) {
            log.error("Error creating test.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteTest(@PathVariable long id, Principal principal) {
        try {
            testService.deleteTest(id, principal);
            return ResponseEntity.status(HttpStatus.CREATED).body("Test has been created successfully");
        } catch (Exception e) {
            log.error("Error creating test.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating test. " + e.getMessage());
        }
    }
}
