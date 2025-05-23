package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.repository.*;
import com.altester.core.service.TestAttemptService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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
  private final QuestionRepository questionRepository;

  private final TestAttemptDTOMapper dtoMapper;
  private final TestAttemptValidation validationService;

  private final Random random = new Random();
  private final Map<Long, List<Question>> testQuestions = new HashMap<>();

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse startAttempt(Principal principal, StartAttemptRequest request) {
    log.info("[DEMO MODE] User: {} is starting a demo attempt for test: {}",
            principal.getName(), request.getTestId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(request.getTestId());
    validationService.validateStudentTestAccess(student, test);
    validationService.validateTestAvailability(test, student);

    List<Question> selectedQuestions = selectQuestionsForTest(test);
    testQuestions.put(request.getTestId(), selectedQuestions);

    return dtoMapper.buildDemoQuestionResponse(test, student, 1, selectedQuestions);
  }

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse getQuestion(Principal principal, GetQuestionRequest request) {
    log.info("[DEMO MODE] User: {} is requesting question {} for test attempt: {}",
            principal.getName(), request.getQuestionNumber(), request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(request.getAttemptId());
    List<Question> selectedQuestions = testQuestions.get(request.getAttemptId());

    if (selectedQuestions == null) {
      selectedQuestions = selectQuestionsForTest(test);
      testQuestions.put(request.getAttemptId(), selectedQuestions);
    }

    int questionNumber = request.getQuestionNumber();
    if (questionNumber < 1 || questionNumber > selectedQuestions.size()) {
      throw ValidationException.invalidParameter(
              "questionNumber", "Question number must be between 1 and " + selectedQuestions.size());
    }

    return dtoMapper.buildDemoQuestionResponse(test, student, questionNumber, selectedQuestions);
  }

  @Override
  @Transactional
  public void saveAnswer(Principal principal, SaveAnswerRequest request) {
    log.info("[DEMO MODE] Simulating answer save for user: {}, test: {}, question: {}",
            principal.getName(), request.getAttemptId(), request.getAnswer().getQuestionId());
  }

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse nextQuestion(Principal principal, NextQuestionRequest request) {
    log.info("[DEMO MODE] User: {} is moving to next question for test: {}, current question: {}",
            principal.getName(), request.getAttemptId(), request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(request.getAttemptId());
    List<Question> selectedQuestions = testQuestions.get(request.getAttemptId());

    if (selectedQuestions == null) {
      selectedQuestions = selectQuestionsForTest(test);
      testQuestions.put(request.getAttemptId(), selectedQuestions);
    }

    int nextQuestionNumber = request.getCurrentQuestionNumber() + 1;

    if (nextQuestionNumber > selectedQuestions.size()) {
      throw ValidationException.invalidParameter(
              "currentQuestionNumber", "Already at the last question");
    }

    return dtoMapper.buildDemoQuestionResponse(test, student, nextQuestionNumber, selectedQuestions);
  }

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse previousQuestion(Principal principal, PreviousQuestionRequest request) {
    log.info("[DEMO MODE] User: {} is moving to previous question for test: {}, current question: {}",
            principal.getName(), request.getAttemptId(), request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(request.getAttemptId());
    List<Question> selectedQuestions = testQuestions.get(request.getAttemptId());

    if (selectedQuestions == null) {
      selectedQuestions = selectQuestionsForTest(test);
      testQuestions.put(request.getAttemptId(), selectedQuestions);
    }

    int prevQuestionNumber = request.getCurrentQuestionNumber() - 1;

    if (prevQuestionNumber < 1) {
      throw ValidationException.invalidParameter(
              "currentQuestionNumber", "Already at the first question");
    }

    return dtoMapper.buildDemoQuestionResponse(test, student, prevQuestionNumber, selectedQuestions);
  }

  @Override
  @Transactional(readOnly = true)
  public AttemptResultResponse completeAttempt(Principal principal, CompleteAttemptRequest request) {
    log.info("[DEMO MODE] User: {} is completing demo test: {}",
            principal.getName(), request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(request.getAttemptId());
    List<Question> selectedQuestions = testQuestions.get(request.getAttemptId());

    if (selectedQuestions == null) {
      selectedQuestions = selectQuestionsForTest(test);
    }

    int randomAiScore = generateRandomAiScore(test);

    return dtoMapper.buildDemoAttemptResult(test, student, selectedQuestions, randomAiScore);
  }

  @Override
  @Transactional(readOnly = true)
  public AttemptStatusResponse getAttemptStatus(Principal principal, Long attemptId) {
    log.info("[DEMO MODE] User: {} is checking status of demo test: {}",
            principal.getName(), attemptId);

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestByIdWithQuestions(attemptId);
    List<Question> selectedQuestions = testQuestions.get(attemptId);

    if (selectedQuestions == null) {
      selectedQuestions = selectQuestionsForTest(test);
      testQuestions.put(attemptId, selectedQuestions);
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(random.nextInt(30) + 5);
    LocalDateTime expirationTime = startTime.plusMinutes(test.getDuration());
    boolean isExpired = now.isAfter(expirationTime);

    List<QuestionAnswerStatus> questionStatuses = buildDemoQuestionStatusList(selectedQuestions);
    int lastQuestionViewed = random.nextInt(selectedQuestions.size()) + 1;
    int timeRemainingSeconds = isExpired ? 0 : random.nextInt(test.getDuration() * 60);
    int answeredQuestions = random.nextInt(selectedQuestions.size() + 1);

    return AttemptStatusResponse.builder()
            .attemptId(attemptId)
            .testTitle(test.getTitle() + " (DEMO)")
            .duration(test.getDuration())
            .startTime(startTime)
            .endTime(expirationTime)
            .isActive(!isExpired)
            .isCompleted(false)
            .isExpired(isExpired)
            .timeRemainingSeconds(timeRemainingSeconds)
            .totalQuestions(selectedQuestions.size())
            .answeredQuestions(answeredQuestions)
            .questionStatuses(questionStatuses)
            .lastQuestionViewed(lastQuestionViewed)
            .build();
  }

  private List<Question> selectQuestionsForTest(Test test) {
    Map<QuestionDifficulty, List<Question>> questionsByDifficulty =
            test.getQuestions().stream().collect(Collectors.groupingBy(Question::getDifficulty));

    List<Question> selectedQuestions = new ArrayList<>();

    if (test.getEasyQuestionsCount() != null && test.getEasyQuestionsCount() > 0) {
      List<Question> easyQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.EASY, new ArrayList<>());
      selectedQuestions.addAll(selectRandomQuestions(easyQuestions, test.getEasyQuestionsCount()));
    }

    if (test.getMediumQuestionsCount() != null && test.getMediumQuestionsCount() > 0) {
      List<Question> mediumQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.MEDIUM, new ArrayList<>());
      selectedQuestions.addAll(selectRandomQuestions(mediumQuestions, test.getMediumQuestionsCount()));
    }

    if (test.getHardQuestionsCount() != null && test.getHardQuestionsCount() > 0) {
      List<Question> hardQuestions = questionsByDifficulty.getOrDefault(QuestionDifficulty.HARD, new ArrayList<>());
      selectedQuestions.addAll(selectRandomQuestions(hardQuestions, test.getHardQuestionsCount()));
    }

    Collections.shuffle(selectedQuestions);

    log.info("[DEMO MODE] Selected {} questions for test {}: {} easy, {} medium, {} hard",
            selectedQuestions.size(), test.getId(),
            test.getEasyQuestionsCount() != null ? test.getEasyQuestionsCount() : 0,
            test.getMediumQuestionsCount() != null ? test.getMediumQuestionsCount() : 0,
            test.getHardQuestionsCount() != null ? test.getHardQuestionsCount() : 0);

    return selectedQuestions;
  }

  private List<Question> selectRandomQuestions(List<Question> questions, int count) {
    if (questions.isEmpty() || count <= 0) {
      return new ArrayList<>();
    }

    if (count >= questions.size()) {
      return new ArrayList<>(questions);
    }

    List<Question> shuffled = new ArrayList<>(questions);
    Collections.shuffle(shuffled);
    return shuffled.subList(0, count);
  }

  private int generateRandomAiScore(Test test) {
    int totalMaxScore = 0;

    if (test.getEasyQuestionsCount() != null && test.getEasyQuestionsCount() > 0) {
      int easyMaxScore = test.getEasyQuestionScore() * test.getEasyQuestionsCount();
      totalMaxScore += random.nextInt(easyMaxScore + 1);
    }

    if (test.getMediumQuestionsCount() != null && test.getMediumQuestionsCount() > 0) {
      int mediumMaxScore = test.getMediumQuestionScore() * test.getMediumQuestionsCount();
      totalMaxScore += random.nextInt(mediumMaxScore + 1);
    }

    if (test.getHardQuestionsCount() != null && test.getHardQuestionsCount() > 0) {
      int hardMaxScore = test.getHardQuestionScore() * test.getHardQuestionsCount();
      totalMaxScore += random.nextInt(hardMaxScore + 1);
    }

    int finalScore = Math.max(1, totalMaxScore);

    log.info("[DEMO MODE] Generated random AI score: {} for test {}", finalScore, test.getId());

    return finalScore;
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

  private Test getTestByIdWithQuestions(Long testId) {
    return testRepository.findByIdWithQuestionsAndOptions(testId)
            .orElseThrow(() -> ResourceNotFoundException.test(testId));
  }

  private User getUserFromPrincipal(Principal principal) {
    return userRepository
            .findByUsername(principal.getName())
            .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
  }
}