package com.altester.core.controller;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import com.altester.core.service.test.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/questions")
@Slf4j
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping(value = "/tests/{testId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addQuestion(
            @PathVariable Long testId,
            @RequestPart("questionData") CreateQuestionDTO createQuestionDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) {

        try {
            QuestionDetailsDTO result = questionService.addQuestion(testId, createQuestionDTO, principal, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error adding question to test {}: {}", testId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long questionId,
            @RequestPart("questionData") UpdateQuestionDTO updateQuestionDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) {

        try {
            QuestionDetailsDTO result = questionService.updateQuestion(questionId, updateQuestionDTO, principal, image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating question {}: {}", questionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Long questionId,
            Principal principal) {

        try {
            questionService.deleteQuestion(questionId, principal);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting question {}: {}", questionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<?> getQuestion(
            @PathVariable Long questionId,
            Principal principal) {

        try {
            QuestionDetailsDTO result = questionService.getQuestion(questionId, principal);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving question {}: {}", questionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{questionId}/position/{newPosition}")
    public ResponseEntity<?> changeQuestionPosition(
            @PathVariable Long questionId,
            @PathVariable int newPosition,
            Principal principal) {

        try {
            questionService.changeQuestionPosition(questionId, newPosition, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
