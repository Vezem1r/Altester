package com.altester.core.service;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import org.springframework.web.multipart.MultipartFile;
import com.altester.core.exception.*;

import java.security.Principal;

public interface QuestionService {

    /**
     * Adds a new question to a test with optional image attachment.
     *
     * @param testId ID of the test to add the question to
     * @param createQuestionDTO DTO containing question details (text, type, options, etc.)
     * @param principal The authenticated user (admin or teacher)
     * @param image Optional image file to attach to the question
     * @return Created question details
     * @throws ResourceNotFoundException if the test or user doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to modify the test
     * @throws StateConflictException if trying to modify a test in a past semester group
     * @throws ValidationException if question data is invalid
     */
    QuestionDetailsDTO addQuestion(Long testId, CreateQuestionDTO createQuestionDTO,
                                   Principal principal, MultipartFile image);

    /**
     * Updates an existing question with new information and optional image replacement.
     *
     * @param questionId ID of the question to update
     * @param updateQuestionDTO DTO containing updated question details
     * @param principal The authenticated user (admin or teacher)
     * @param image Optional new image file to replace the existing one
     * @return Updated question details
     * @throws ResourceNotFoundException if the question or user doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to modify the test
     * @throws StateConflictException if trying to modify a test in a past semester group
     * @throws ValidationException if updated question data is invalid
     */
    QuestionDetailsDTO updateQuestion(Long questionId, UpdateQuestionDTO updateQuestionDTO,
                                      Principal principal, MultipartFile image);

    /**
     * Deletes a question from a test and reorders remaining questions.
     *
     * @param questionId ID of the question to delete
     * @param principal The authenticated user (admin or teacher)
     * @throws ResourceNotFoundException if the question or user doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to modify the test
     * @throws StateConflictException if trying to modify a test in a past semester group
     */
    void deleteQuestion(Long questionId, Principal principal);

    /**
     * Retrieves detailed information for a specific question.
     *
     * @param questionId ID of the question to retrieve
     * @param principal The authenticated user (admin or teacher)
     * @return Question details including options and image path
     * @throws ResourceNotFoundException if the question or user doesn't exist
     * @throws AccessDeniedException if the user doesn't have permission to access the test
     */
    QuestionDetailsDTO getQuestion(Long questionId, Principal principal);
}