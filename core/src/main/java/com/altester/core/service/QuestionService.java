package com.altester.core.service;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface QuestionService {

    QuestionDetailsDTO addQuestion(Long testId, CreateQuestionDTO createQuestionDTO,
                                   Principal principal, MultipartFile image);

    QuestionDetailsDTO updateQuestion(Long questionId, UpdateQuestionDTO updateQuestionDTO,
                                      Principal principal, MultipartFile image);

    void deleteQuestion(Long questionId, Principal principal);

    QuestionDetailsDTO getQuestion(Long questionId, Principal principal);

    void changeQuestionPosition(Long questionId, int newPosition, Principal principal);
}