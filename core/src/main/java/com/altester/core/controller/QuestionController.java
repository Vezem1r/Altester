package com.altester.core.controller;

import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.service.QuestionService;
import com.altester.core.util.FileValidator;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions")
@Slf4j
@RequiredArgsConstructor
@Validated
public class QuestionController {

  private final QuestionService questionService;
  private final FileValidator fileValidator;

  @GetMapping("/{questionId}")
  public ResponseEntity<QuestionDetailsDTO> getQuestion(
      @PathVariable Long questionId, Principal principal) {
    QuestionDetailsDTO result = questionService.getQuestion(questionId, principal);
    return ResponseEntity.ok(result);
  }
}
