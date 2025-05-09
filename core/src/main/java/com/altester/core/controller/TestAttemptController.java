package com.altester.core.controller;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.service.TestAttemptService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/test-attempts")
@Slf4j
@Validated
@PreAuthorize("hasRole('STUDENT')")
public class TestAttemptController {

  private final TestAttemptService testAttemptService;

  @PostMapping("/start")
  public ResponseEntity<SingleQuestionResponse> startAttempt(
      Principal principal, @RequestBody StartAttemptRequest request) {
    SingleQuestionResponse response = testAttemptService.startAttempt(principal, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/question/{attemptId}/{questionNumber}")
  public ResponseEntity<SingleQuestionResponse> getQuestion(
      Principal principal, @PathVariable long attemptId, @PathVariable int questionNumber) {
    GetQuestionRequest request =
        GetQuestionRequest.builder().attemptId(attemptId).questionNumber(questionNumber).build();
    SingleQuestionResponse response = testAttemptService.getQuestion(principal, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/save-answer")
  public ResponseEntity<Void> saveAnswer(
      Principal principal, @RequestBody SaveAnswerRequest request) {
    testAttemptService.saveAnswer(principal, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/next-question")
  public ResponseEntity<SingleQuestionResponse> nextQuestion(
      Principal principal, @RequestBody NextQuestionRequest request) {
    SingleQuestionResponse response = testAttemptService.nextQuestion(principal, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/previous-question")
  public ResponseEntity<SingleQuestionResponse> previousQuestion(
      Principal principal, @RequestBody PreviousQuestionRequest request) {
    SingleQuestionResponse response = testAttemptService.previousQuestion(principal, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/complete")
  public ResponseEntity<AttemptResultResponse> completeAttempt(
      Principal principal, @RequestBody CompleteAttemptRequest request) {
    AttemptResultResponse response = testAttemptService.completeAttempt(principal, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/status/{attemptId}")
  public ResponseEntity<AttemptStatusResponse> getAttemptStatus(
      Principal principal, @PathVariable long attemptId) {
    AttemptStatusResponse response = testAttemptService.getAttemptStatus(principal, attemptId);
    return ResponseEntity.ok(response);
  }
}
