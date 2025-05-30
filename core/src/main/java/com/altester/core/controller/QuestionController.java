package com.altester.core.controller;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import com.altester.core.service.QuestionService;
import com.altester.core.util.FileValidator;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/questions")
@Slf4j
@RequiredArgsConstructor
@Validated
public class QuestionController {

  private final QuestionService questionService;
  private final FileValidator fileValidator;

  @PostMapping(value = "/tests/{testId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QuestionDetailsDTO> addQuestion(
      @PathVariable Long testId,
      @Valid @RequestPart("questionData") CreateQuestionDTO createQuestionDTO,
      @RequestPart(value = "image", required = false) MultipartFile image,
      Principal principal) {

    log.info(
        "User {} adding question to test ID: {}, question type: {}",
        principal.getName(),
        testId,
        createQuestionDTO.getQuestionType());

    if (image != null) {
      fileValidator.validateImage(image);
      log.info(
          "Image included with size: {} bytes, content type: {}",
          image.getSize(),
          image.getContentType());
    }

    QuestionDetailsDTO result =
        questionService.addQuestion(testId, createQuestionDTO, principal, image);
    log.info(
        "Question successfully added to test ID: {}, new question ID: {}", testId, result.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PutMapping(value = "/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QuestionDetailsDTO> updateQuestion(
      @PathVariable Long questionId,
      @Valid @RequestPart("questionData") UpdateQuestionDTO updateQuestionDTO,
      @RequestPart(value = "image", required = false) MultipartFile image,
      Principal principal) {

    log.info("User {} updating question ID: {}", principal.getName(), questionId);

    if (image != null) {
      fileValidator.validateImage(image);
      log.info(
          "New image included with size: {} bytes, content type: {}",
          image.getSize(),
          image.getContentType());
    }

    QuestionDetailsDTO result =
        questionService.updateQuestion(questionId, updateQuestionDTO, principal, image);
    log.info("Question ID: {} successfully updated", questionId);

    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{questionId}")
  public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId, Principal principal) {
    questionService.deleteQuestion(questionId, principal);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{questionId}")
  public ResponseEntity<QuestionDetailsDTO> getQuestion(
      @PathVariable Long questionId, Principal principal) {
    QuestionDetailsDTO result = questionService.getQuestion(questionId, principal);
    return ResponseEntity.ok(result);
  }
}
