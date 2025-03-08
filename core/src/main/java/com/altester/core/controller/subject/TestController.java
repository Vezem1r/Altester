package com.altester.core.controller.subject;

import com.altester.core.dtos.core_service.subject.CreateTestDTO;
import com.altester.core.service.subject.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/teacher/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/create")
    public ResponseEntity<String> createTest(@Valid @RequestBody CreateTestDTO createTestDTO, Principal principal) {
        try {
            testService.createTest(createTestDTO, principal);
            return ResponseEntity.status(HttpStatus.CREATED).body("Test has been created successfully");
        } catch (Exception e) {
            log.error("Error creating test.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating test. " + e.getMessage());
        }
    }
}
