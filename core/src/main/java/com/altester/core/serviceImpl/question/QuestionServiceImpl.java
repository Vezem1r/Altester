package com.altester.core.serviceImpl.question;

import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.*;
import com.altester.core.service.QuestionService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.test.TestAccessValidator;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
  private final QuestionRepository questionRepository;
  private final TestRepository testRepository;
  private final UserRepository userRepository;
  private final OptionRepository optionRepository;
  private final TestAccessValidator testAccessValidator;
  private final QuestionDTOMapper questionDTOMapper;
  private final CacheService cacheService;

  private static final Integer DEFAULT_EASY_SCORE = 5;
  private static final Integer DEFAULT_MEDIUM_SCORE = 8;
  private static final Integer DEFAULT_HARD_SCORE = 10;

  private User getCurrentUser(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(
            () -> {
              log.error("User {} not found", principal.getName());
              return ResourceNotFoundException.user(principal.getName());
            });
  }

  private Question getQuestionById(Long questionId) {
    return questionRepository
        .findById(questionId)
        .orElseThrow(
            () -> {
              log.error("Question with ID {} not found", questionId);
              return ResourceNotFoundException.question(questionId);
            });
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "question", key = "'id:' + #questionId")
  public QuestionDetailsDTO getQuestion(Long questionId, Principal principal) {
    log.info("User {} is attempting to get question with ID {}", principal.getName(), questionId);

    User currentUser = getCurrentUser(principal);
    Question question = getQuestionById(questionId);
    Test test = question.getTest();

    testAccessValidator.validateTestAccess(currentUser, test);

    return questionDTOMapper.convertToQuestionDetailsDTO(question);
  }
}
