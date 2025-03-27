package com.altester.core.controller;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import com.altester.core.service.QuestionService;
import com.altester.core.validation.FileValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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

        log.info("User {} adding question to test ID: {}, question type: {}",
                principal.getName(), testId, createQuestionDTO.getQuestionType());

        if (image != null) {
            fileValidator.validateImage(image);
            log.info("Image included with size: {} bytes, content type: {}",
                    image.getSize(), image.getContentType());
        }

        QuestionDetailsDTO result = questionService.addQuestion(testId, createQuestionDTO, principal, image);
        log.info("Question successfully added to test ID: {}, new question ID: {}",
                testId, result.getId());

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
            log.info("New image included with size: {} bytes, content type: {}",
                    image.getSize(), image.getContentType());
        }

        QuestionDetailsDTO result = questionService.updateQuestion(questionId, updateQuestionDTO, principal, image);
        log.info("Question ID: {} successfully updated", questionId);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            Principal principal) {

        log.info("User {} deleting question ID: {}", principal.getName(), questionId);
        questionService.deleteQuestion(questionId, principal);
        log.info("Question ID: {} successfully deleted", questionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailsDTO> getQuestion(
            @PathVariable Long questionId,
            Principal principal) {

        log.info("User {} retrieving question ID: {}", principal.getName(), questionId);
        QuestionDetailsDTO result = questionService.getQuestion(questionId, principal);
        log.info("Question ID: {} successfully retrieved", questionId);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{questionId}/position/{newPosition}")
    public ResponseEntity<Void> changeQuestionPosition(
            @PathVariable Long questionId,
            @PathVariable @Min(value = 0, message = "Position cannot be negative") int newPosition,
            Principal principal) {

        log.info("User {} changing position of question ID: {} to position: {}",
                principal.getName(), questionId, newPosition);
        questionService.changeQuestionPosition(questionId, newPosition, principal);
        log.info("Position of question ID: {} successfully changed to position: {}",
                questionId, newPosition);

        return ResponseEntity.ok().build();
    }
}