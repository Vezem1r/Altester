package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.repository.*;
import com.altester.core.service.TestAttemptService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestAttemptServiceImpl implements TestAttemptService {

  private final UserRepository userRepository;
  private final TestRepository testRepository;
  private final AttemptRepository attemptRepository;
  private final QuestionRepository questionRepository;

  private final TestAttemptDTOMapper dtoMapper;
  private final TestAttemptValidation validationService;
  private final AttemptQuestionService questionService;

  private final Random random = new Random();

  @Override
  @Transactional
  public SingleQuestionResponse startAttempt(Principal principal, StartAttemptRequest request) {
    log.info(
        "[DEMO MODE] User: {} is starting a demo attempt for test: {}",
        principal.getName(),
        request.getTestId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestById(request.getTestId());
    validationService.validateStudentTestAccess(student, test);
    validationService.validateTestAvailability(test, student);

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);

    Attempt demoAttempt = createDemoAttempt(test, student);

    return dtoMapper.getQuestionByNumber(demoAttempt, 1, questionsForAttempt);
  }

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse getQuestion(Principal principal, GetQuestionRequest request) {
    log.info(
        "[DEMO MODE] User: {} is requesting question {} for demo attempt: {}",
        principal.getName(),
        request.getQuestionNumber(),
        request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getDemoAttemptById(request.getAttemptId());
    Test test = attempt.getTest();

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);

    int questionNumber = request.getQuestionNumber();
    if (questionNumber < 1 || questionNumber > questionsForAttempt.size()) {
      throw ValidationException.invalidParameter(
          "questionNumber", "Question number must be between 1 and " + questionsForAttempt.size());
    }

    return dtoMapper.getQuestionByNumber(attempt, questionNumber, questionsForAttempt);
  }

  @Override
  @Transactional
  public void saveAnswer(Principal principal, SaveAnswerRequest request) {
    log.info(
        "[DEMO MODE] Simulating answer save for user: {}, attempt: {}, question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getAnswer().getQuestionId());
  }

  @Override
  @Transactional
  public SingleQuestionResponse nextQuestion(Principal principal, NextQuestionRequest request) {
    log.info(
        "[DEMO MODE] User: {} is moving to next question for demo attempt: {}, current question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getDemoAttemptById(request.getAttemptId());
    Test test = attempt.getTest();

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);
    int nextQuestionNumber = request.getCurrentQuestionNumber() + 1;

    if (nextQuestionNumber > questionsForAttempt.size()) {
      throw ValidationException.invalidParameter(
          "currentQuestionNumber", "Already at the last question");
    }

    return dtoMapper.getQuestionByNumber(attempt, nextQuestionNumber, questionsForAttempt);
  }

  @Override
  @Transactional
  public SingleQuestionResponse previousQuestion(
      Principal principal, PreviousQuestionRequest request) {
    log.info(
        "[DEMO MODE] User: {} is moving to previous question for demo attempt: {}, current question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getDemoAttemptById(request.getAttemptId());
    Test test = attempt.getTest();

    int prevQuestionNumber = request.getCurrentQuestionNumber() - 1;

    if (prevQuestionNumber < 1) {
      throw ValidationException.invalidParameter(
          "currentQuestionNumber", "Already at the first question");
    }

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);

    return dtoMapper.getQuestionByNumber(attempt, prevQuestionNumber, questionsForAttempt);
  }

  @Override
  @Transactional
  public AttemptResultResponse completeAttempt(
      Principal principal, CompleteAttemptRequest request) {
    log.info(
        "[DEMO MODE] User: {} is completing demo attempt: {}",
        principal.getName(),
        request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getDemoAttemptById(request.getAttemptId());
    Test test = attempt.getTest();

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);

    int randomScore = generateRandomScore(test);
    int randomAiScore = generateRandomAiScore(test, randomScore);

    attempt.setStatus(AttemptStatus.AI_REVIEWED);
    attempt.setEndTime(LocalDateTime.now());
    attempt.setScore(randomScore);
    attempt.setAiScore(randomAiScore);

    return dtoMapper.buildDemoAttemptResult(attempt, questionsForAttempt);
  }

  @Override
  @Transactional(readOnly = true)
  public AttemptStatusResponse getAttemptStatus(Principal principal, Long attemptId) {
    log.info(
        "[DEMO MODE] User: {} is checking status of demo attempt: {}",
        principal.getName(),
        attemptId);

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getDemoAttemptById(attemptId);
    Test test = attempt.getTest();

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expirationTime = attempt.getStartTime().plusMinutes(test.getDuration());
    boolean isExpired = now.isAfter(expirationTime);

    List<QuestionAnswerStatus> questionStatuses = buildDemoQuestionStatusList(questionsForAttempt);
    int lastQuestionViewed = random.nextInt(questionsForAttempt.size()) + 1;
    int timeRemainingSeconds = isExpired ? 0 : random.nextInt(3600);
    int answeredQuestions = random.nextInt(questionsForAttempt.size() + 1);

    return AttemptStatusResponse.builder()
        .attemptId(attempt.getId())
        .testTitle(test.getTitle())
        .duration(test.getDuration())
        .startTime(attempt.getStartTime())
        .endTime(expirationTime)
        .isActive(attempt.getStatus() == AttemptStatus.IN_PROGRESS && !isExpired)
        .isCompleted(attempt.getStatus() == AttemptStatus.COMPLETED)
        .isExpired(isExpired)
        .timeRemainingSeconds(timeRemainingSeconds)
        .totalQuestions(questionsForAttempt.size())
        .answeredQuestions(answeredQuestions)
        .questionStatuses(questionStatuses)
        .lastQuestionViewed(lastQuestionViewed)
        .build();
  }

  private Attempt createDemoAttempt(Test test, User student) {
    return Attempt.builder()
        .id(System.currentTimeMillis())
        .test(test)
        .student(student)
        .attemptNumber(1)
        .startTime(LocalDateTime.now())
        .status(AttemptStatus.IN_PROGRESS)
        .submissions(new ArrayList<>())
        .build();
  }

  private Attempt getDemoAttemptById(Long attemptId) {
    Test test = testRepository.findAll().getFirst();
    User student = userRepository.findByUsername("student").orElseThrow();

    return Attempt.builder()
        .id(attemptId)
        .test(test)
        .student(student)
        .attemptNumber(1)
        .startTime(LocalDateTime.now().minusMinutes(10))
        .status(AttemptStatus.IN_PROGRESS)
        .submissions(new ArrayList<>())
        .build();
  }

  private int generateRandomScore(Test test) {
    int totalPossibleScore = test.getTotalScore();
    return random.nextInt(totalPossibleScore + 1);
  }

  private int generateRandomAiScore(Test test, int baseScore) {
    int totalPossibleScore = test.getTotalScore();
    int maxAiBonus = Math.min(totalPossibleScore - baseScore, totalPossibleScore / 4);
    int aiBonus = random.nextInt(maxAiBonus + 1);
    return baseScore + aiBonus;
  }

  private List<QuestionAnswerStatus> buildDemoQuestionStatusList(List<Question> questions) {
    List<QuestionAnswerStatus> questionStatuses = new ArrayList<>();

    for (int i = 0; i < questions.size(); i++) {
      Question question = questions.get(i);
      boolean isAnswered = random.nextBoolean();

      questionStatuses.add(
          QuestionAnswerStatus.builder()
              .questionNumber(i + 1)
              .questionId(question.getId())
              .isAnswered(isAnswered)
              .build());
    }

    return questionStatuses;
  }

  private Test getTestById(Long testId) {
    return testRepository
        .findById(testId)
        .orElseThrow(() -> ResourceNotFoundException.test(testId));
  }

  private User getUserFromPrincipal(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
  }
}
