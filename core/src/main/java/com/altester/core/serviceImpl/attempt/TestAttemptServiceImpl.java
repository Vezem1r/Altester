package com.altester.core.serviceImpl.attempt;

import com.altester.core.dtos.ai_service.GradingResponse;
import com.altester.core.dtos.core_service.attempt.*;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.AttemptStatus;
import com.altester.core.model.subject.enums.QuestionType;
import com.altester.core.repository.*;
import com.altester.core.service.AiGradingService;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.service.TestAttemptService;
import com.altester.core.serviceImpl.CacheService;
import jakarta.persistence.OptimisticLockException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
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
  private final CacheService cacheService;
  private final AiGradingService aiGradingService;
  private final NotificationDispatchService notificationDispatchService;

  private final TestAttemptDTOMapper dtoMapper;
  private final TestAttemptValidation validationService;
  private final AttemptAutoGrading gradingService;
  private final AttemptQuestionService questionService;

  private static final int AI_GRADING_TIMEOUT_SEC = 10;

  @Override
  @Transactional
  public SingleQuestionResponse startAttempt(Principal principal, StartAttemptRequest request) {
    log.info(
        "[START ATTEMPT] User: {} is starting a new attempt for test: {}",
        principal.getName(),
        request.getTestId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Test test = getTestById(request.getTestId());
    validationService.validateStudentTestAccess(student, test);

    List<Attempt> attempts = attemptRepository.findByTestAndStudent(test, student);
    Attempt activeAttempt =
        attempts.stream()
            .filter(attempt -> attempt.getStatus() == AttemptStatus.IN_PROGRESS)
            .findFirst()
            .orElse(null);

    if (activeAttempt != null) {
      log.info(
          "[RECONNECT ATTEMPT] User: {} is reconnecting to an existing attempt: {}",
          principal.getName(),
          activeAttempt.getId());

      if (validationService.isAttemptExpired(activeAttempt)) {
        autoCompleteExpiredAttempt(activeAttempt);
      } else {
        List<Question> questionsForAttempt =
            questionService.getQuestionsFromSubmissions(activeAttempt);
        if (questionsForAttempt.isEmpty()) {
          questionsForAttempt = questionService.getQuestionsForTest(test);
        }

        int questionToResume =
            questionService.findQuestionToResume(activeAttempt, questionsForAttempt);
        return dtoMapper.getQuestionByNumber(activeAttempt, questionToResume, questionsForAttempt);
      }
    }

    validationService.validateTestAvailability(test, student);

    int attemptNumber =
        (int) attempts.stream().filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS).count() + 1;

    Attempt attempt =
        Attempt.builder()
            .test(test)
            .student(student)
            .attemptNumber(attemptNumber)
            .startTime(LocalDateTime.now())
            .status(AttemptStatus.IN_PROGRESS)
            .submissions(new ArrayList<>())
            .build();

    List<Question> questionsForAttempt = questionService.getQuestionsForTest(test);
    questionService.createInitialSubmissions(attempt, questionsForAttempt);

    attempt = attemptRepository.save(attempt);

    cacheService.clearAttemptRelatedCaches();
    cacheService.clearStudentRelatedCaches();

    return dtoMapper.getQuestionByNumber(attempt, 1, questionsForAttempt);
  }

  @Override
  @Transactional(readOnly = true)
  public SingleQuestionResponse getQuestion(Principal principal, GetQuestionRequest request) {
    log.info(
        "[GET QUESTION] User: {} is requesting question {} for attempt: {}",
        principal.getName(),
        request.getQuestionNumber(),
        request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getAttemptById(request.getAttemptId());
    validationService.validateAttemptOwnership(attempt, student);

    if (!attempt.getStatus().equals(AttemptStatus.COMPLETED)) {
      if (validationService.isAttemptExpired(attempt)) {
        autoCompleteExpiredAttempt(attempt);
        throw new StateConflictException(
            "attempt", "expired", "This attempt has expired. The time limit has been reached.");
      }
    } else {
      throw new StateConflictException(
          "attempt", "completed", "This attempt has already been completed.");
    }

    List<Question> questionsForAttempt = questionService.getQuestionsFromSubmissions(attempt);

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
        "[SAVE ANSWER] User: {} is saving answer for attempt: {}, question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getAnswer().getQuestionId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getAttemptById(request.getAttemptId());
    validationService.validateAttemptOwnership(attempt, student);

    if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
      if (validationService.isAttemptExpired(attempt)) {
        autoCompleteExpiredAttempt(attempt);
        throw new StateConflictException(
            "attempt", "expired", "This attempt has expired. The time limit has been reached.");
      }
    } else {
      throw new StateConflictException(
          "attempt", "completed", "This attempt has already been completed.");
    }

    AnswerDTO answerDTO = request.getAnswer();
    Question question = getQuestionById(answerDTO.getQuestionId());

    if (attempt.getSubmissions() == null) {
      attempt.setSubmissions(new ArrayList<>());
    }

    Submission existingSubmission =
        attempt.getSubmissions().stream()
            .filter(s -> s.getQuestion().getId() == question.getId())
            .findFirst()
            .orElse(null);

    if (existingSubmission != null) {
      questionService.updateSubmission(existingSubmission, answerDTO, question);
    } else {
      Submission submission =
          Submission.builder()
              .attempt(attempt)
              .question(question)
              .selectedOptions(new ArrayList<>())
              .build();

      questionService.updateSubmission(submission, answerDTO, question);
      attempt.getSubmissions().add(submission);
    }

    attemptRepository.save(attempt);
    cacheService.clearAttemptRelatedCaches();
  }

  @Override
  @Transactional
  public SingleQuestionResponse nextQuestion(Principal principal, NextQuestionRequest request) {
    log.info(
        "[NEXT QUESTION] User: {} is moving to next question for attempt: {}, current question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getAttemptById(request.getAttemptId());
    validationService.validateAttemptOwnership(attempt, student);

    if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
      if (validationService.isAttemptExpired(attempt)) {
        autoCompleteExpiredAttempt(attempt);
        throw new StateConflictException(
            "attempt", "expired", "This attempt has expired. The time limit has been reached.");
      }
    } else {
      throw new StateConflictException(
          "attempt", "completed", "This attempt has already been completed.");
    }

    if (request.getCurrentAnswer() != null) {
      SaveAnswerRequest saveRequest =
          SaveAnswerRequest.builder()
              .attemptId(request.getAttemptId())
              .answer(request.getCurrentAnswer())
              .build();
      saveAnswer(principal, saveRequest);
    }

    List<Question> questionsForAttempt = questionService.getQuestionsFromSubmissions(attempt);
    int nextQuestionNumber = request.getCurrentQuestionNumber() + 1;

    if (nextQuestionNumber > questionsForAttempt.size()) {
      throw ValidationException.invalidParameter(
          "currentQuestionNumber", "Already at the last question");
    }

    cacheService.clearAttemptRelatedCaches();
    return dtoMapper.getQuestionByNumber(attempt, nextQuestionNumber, questionsForAttempt);
  }

  @Override
  @Transactional
  public SingleQuestionResponse previousQuestion(
      Principal principal, PreviousQuestionRequest request) {
    log.info(
        "[PREVIOUS QUESTION] User: {} is moving to previous question for attempt: {}, current question: {}",
        principal.getName(),
        request.getAttemptId(),
        request.getCurrentQuestionNumber());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getAttemptById(request.getAttemptId());
    validationService.validateAttemptOwnership(attempt, student);

    if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
      if (validationService.isAttemptExpired(attempt)) {
        autoCompleteExpiredAttempt(attempt);
        throw new StateConflictException(
            "attempt", "expired", "This attempt has expired. The time limit has been reached.");
      }
    } else {
      throw new StateConflictException(
          "attempt", "completed", "This attempt has already been completed.");
    }

    if (request.getCurrentAnswer() != null) {
      SaveAnswerRequest saveRequest =
          SaveAnswerRequest.builder()
              .attemptId(request.getAttemptId())
              .answer(request.getCurrentAnswer())
              .build();
      saveAnswer(principal, saveRequest);
    }

    int prevQuestionNumber = request.getCurrentQuestionNumber() - 1;

    if (prevQuestionNumber < 1) {
      throw ValidationException.invalidParameter(
          "currentQuestionNumber", "Already at the first question");
    }

    List<Question> questionsForAttempt = questionService.getQuestionsFromSubmissions(attempt);

    cacheService.clearAttemptRelatedCaches();

    return dtoMapper.getQuestionByNumber(attempt, prevQuestionNumber, questionsForAttempt);
  }

  @Override
  @Transactional
  public AttemptResultResponse completeAttempt(
      Principal principal, CompleteAttemptRequest request) {
    log.info(
        "[COMPLETE ATTEMPT] User: {} is completing attempt: {}",
        principal.getName(),
        request.getAttemptId());

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    String processingKey = "attempt_" + request.getAttemptId();
    ensureNotProcessing(processingKey);

    try {
      Attempt attempt = getAttemptById(request.getAttemptId());
      validationService.validateAttemptOwnership(attempt, student);

      if (isAlreadyCompletedOrReviewed(attempt)) {
        return dtoMapper.buildAttemptResult(attempt);
      }

      checkAiGradingCooldown(attempt);

      int totalScore = gradeMultipleChoiceQuestions(attempt);

      updateAttemptStatus(attempt, totalScore);

      handleAiGrading(attempt, totalScore);

      clearCashes();
      return dtoMapper.buildAttemptResult(attempt);
    } catch (OptimisticLockException | StaleObjectStateException e) {
      log.info(
          "[OPTIMISTIC LOCK] Detected for attempt: {}, returning current state",
          request.getAttemptId());

      throw new StateConflictException(
          "attempt",
          "processing",
          "This attempt is already being submitted and is in the process of being graded. Please wait.");
    } finally {
      cacheService.removeProcessingFlag(processingKey);
    }
  }

  private void ensureNotProcessing(String processingKey) {
    Boolean isProcessing = cacheService.getProcessingFlag(processingKey, Boolean.class);

    if (Boolean.TRUE.equals(isProcessing)) {
      log.debug("[COMPLETE ATTEMPT] Attempt is currently being processed by another request");
      throw new StateConflictException(
          "attempt",
          "processing",
          "This attempt is already being submitted and is in the process of being graded. Please wait.");
    }
    cacheService.putProcessingFlag(processingKey, true, 30);
  }

  private boolean isAlreadyCompletedOrReviewed(Attempt attempt) {
    if (attempt.getStatus() == AttemptStatus.COMPLETED
        || attempt.getStatus() == AttemptStatus.REVIEWED) {
      log.debug(
          "[COMPLETE ATTEMPT] Attempt {} is already {}, returning existing result",
          attempt.getId(),
          attempt.getStatus());
      return true;
    }
    return false;
  }

  private void checkAiGradingCooldown(Attempt attempt) {
    if (attempt.getAiGradingSentAt() != null) {
      LocalDateTime cooldownEndTime = attempt.getAiGradingSentAt().plusMinutes(5);
      if (LocalDateTime.now().isBefore(cooldownEndTime)) {
        throw new StateConflictException(
            "attempt",
            "cooldown",
            "Please wait before submitting again. AI grading is already in progress.");
      }
    }
  }

  private int gradeMultipleChoiceQuestions(Attempt attempt) {
    int totalScore = 0;
    if (attempt.getSubmissions() != null) {
      for (Submission submission : attempt.getSubmissions()) {
        Question question = submission.getQuestion();
        if (isChoiceQuestionType(question.getQuestionType())) {
          totalScore += gradingService.gradeMultipleSelectionQuestion(submission);
        }
      }
    }
    return totalScore;
  }

  private void updateAttemptStatus(Attempt attempt, int totalScore) {
    attempt.setStatus(AttemptStatus.COMPLETED);
    attempt.setEndTime(LocalDateTime.now());
    attempt.setScore(totalScore);
    attempt.setAiGradingSentAt(LocalDateTime.now());
    attemptRepository.save(attempt);
  }

  private void handleAiGrading(Attempt attempt, int finalTotalScore) {
    log.info("[AI GRADING] Sending attempt: {} for AI grading", attempt.getId());
    CompletableFuture<GradingResponse> futureResponse =
        aiGradingService.processAttemptForAiGrading(attempt);

    try {
      processAiGradingResponse(attempt, futureResponse, finalTotalScore);
    } catch (TimeoutException e) {
      handleAiGradingTimeout(attempt, futureResponse, finalTotalScore);
    } catch (InterruptedException e) {
      log.error(
          "[AI GRADING] Thread interrupted while getting AI grading result for attempt: {}: {}",
          attempt.getId(),
          e.getMessage());
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error(
          "[AI GRADING] Error executing AI grading for attempt: {}: {}",
          attempt.getId(),
          e.getMessage());
    }
  }

  private void processAiGradingResponse(
      Attempt attempt, CompletableFuture<GradingResponse> futureResponse, int finalTotalScore)
      throws InterruptedException, ExecutionException, TimeoutException {

    GradingResponse gradingResponse = futureResponse.get(AI_GRADING_TIMEOUT_SEC, TimeUnit.SECONDS);

    if (gradingResponse != null && gradingResponse.isSuccess()) {
      log.info(
          "[AI GRADING] Received successful result for attempt: {} with score: {}",
          attempt.getId(),
          gradingResponse.getAttemptScore());

      attempt.setScore(finalTotalScore + gradingResponse.getAttemptScore());
      attempt.setStatus(AttemptStatus.REVIEWED);

      attemptRepository.save(attempt);
      clearCashes();

      notificationDispatchService.notifyTestGradedByAi(attempt);
    } else {
      log.info("[AI GRADING] Received unsuccessful result for attempt: {}", attempt.getId());
    }
  }

  private void handleAiGradingTimeout(
      Attempt attempt, CompletableFuture<GradingResponse> futureResponse, int finalTotalScore) {

    log.debug(
        "[AI GRADING] Result not available within {} seconds for attempt: {}",
        AI_GRADING_TIMEOUT_SEC,
        attempt.getId());

    futureResponse.thenAccept(
        gradingResponse -> {
          try {
            if (gradingResponse != null && gradingResponse.isSuccess()) {
              Attempt updatedAttempt = getAttemptById(attempt.getId());
              if (updatedAttempt != null) {
                updatedAttempt.setScore(finalTotalScore + gradingResponse.getAttemptScore());
                updatedAttempt.setStatus(AttemptStatus.REVIEWED);
                attemptRepository.save(updatedAttempt);
                notificationDispatchService.notifyTestGradedByAi(updatedAttempt);
                log.debug(
                    "[AI GRADING] Successfully processed delayed result for attempt: {}",
                    attempt.getId());
              }
            }
          } catch (Exception ex) {
            log.error(
                "[AI GRADING] Error processing delayed result for attempt: {}: {}",
                attempt.getId(),
                ex.getMessage());
          }
        });
  }

  @Override
  @Transactional(readOnly = true)
  public AttemptStatusResponse getAttemptStatus(Principal principal, Long attemptId) {
    log.info(
        "[ATTEMPT STATUS] User: {} is checking status of attempt: {}",
        principal.getName(),
        attemptId);

    User student = getUserFromPrincipal(principal);
    validationService.ensureStudentRole(student);

    Attempt attempt = getAttemptById(attemptId);
    validationService.validateAttemptOwnership(attempt, student);

    Test test = attempt.getTest();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expirationTime = attempt.getStartTime().plusMinutes(test.getDuration());
    boolean isExpired = now.isAfter(expirationTime);

    if (isExpired && attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
      autoCompleteExpiredAttempt(attempt);
    }

    List<Question> questionsForAttempt = questionService.getQuestionsFromSubmissions(attempt);

    List<QuestionAnswerStatus> questionStatuses =
        buildQuestionStatusList(attempt, questionsForAttempt);
    int lastQuestionViewed = calculateLastQuestionViewed(attempt, questionsForAttempt);
    int timeRemainingSeconds = calculateTimeRemaining(now, expirationTime, isExpired, attempt);
    int answeredQuestions = countAnsweredQuestions(attempt);

    return buildStatusResponse(
        attempt,
        test,
        expirationTime,
        isExpired,
        questionsForAttempt,
        questionStatuses,
        lastQuestionViewed,
        timeRemainingSeconds,
        answeredQuestions);
  }

  private List<QuestionAnswerStatus> buildQuestionStatusList(
      Attempt attempt, List<Question> questionsForAttempt) {

    List<QuestionAnswerStatus> questionStatuses = new ArrayList<>();

    for (int i = 0; i < questionsForAttempt.size(); i++) {
      Question question = questionsForAttempt.get(i);
      boolean isAnswered = isQuestionAnswered(attempt, question);

      questionStatuses.add(
          QuestionAnswerStatus.builder()
              .questionNumber(i + 1)
              .questionId(question.getId())
              .isAnswered(isAnswered)
              .build());
    }

    return questionStatuses;
  }

  private boolean isQuestionAnswered(Attempt attempt, Question question) {
    if (attempt.getSubmissions() == null) {
      return false;
    }

    return attempt.getSubmissions().stream()
        .filter(s -> s.getQuestion().getId() == question.getId())
        .anyMatch(this::hasAnswer);
  }

  private boolean hasAnswer(Submission s) {
    return (s.getSelectedOptions() != null && !s.getSelectedOptions().isEmpty())
        || (s.getAnswerText() != null && !s.getAnswerText().isEmpty());
  }

  private int calculateLastQuestionViewed(Attempt attempt, List<Question> questionsForAttempt) {
    if (attempt.getSubmissions() == null || attempt.getSubmissions().isEmpty()) {
      return 1;
    }

    Optional<Submission> lastAnsweredSubmission =
        attempt.getSubmissions().stream()
            .filter(this::hasAnswer)
            .max(
                Comparator.comparing(
                    s -> questionService.findQuestionIndex(questionsForAttempt, s.getQuestion())));

    if (lastAnsweredSubmission.isPresent()) {
      int lastIndex =
          questionService.findQuestionIndex(
              questionsForAttempt, lastAnsweredSubmission.get().getQuestion());
      return lastIndex + 1;
    }

    return 1;
  }

  private int calculateTimeRemaining(
      LocalDateTime now, LocalDateTime expirationTime, boolean isExpired, Attempt attempt) {

    if (isExpired || attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
      return 0;
    }

    int timeRemainingSeconds = (int) Duration.between(now, expirationTime).getSeconds();
    return Math.max(timeRemainingSeconds, 0);
  }

  private int countAnsweredQuestions(Attempt attempt) {
    if (attempt.getSubmissions() == null) {
      return 0;
    }

    return (int) attempt.getSubmissions().stream().filter(this::hasAnswer).count();
  }

  private AttemptStatusResponse buildStatusResponse(
      Attempt attempt,
      Test test,
      LocalDateTime expirationTime,
      boolean isExpired,
      List<Question> questionsForAttempt,
      List<QuestionAnswerStatus> questionStatuses,
      int lastQuestionViewed,
      int timeRemainingSeconds,
      int answeredQuestions) {

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

  @Transactional
  public void autoCompleteExpiredAttempt(Attempt attempt) {
    attempt.setStatus(AttemptStatus.COMPLETED);
    LocalDateTime expirationTime =
        attempt.getStartTime().plusMinutes(attempt.getTest().getDuration());
    attempt.setEndTime(expirationTime);

    int totalScore = 0;

    if (attempt.getSubmissions() != null) {
      for (Submission submission : attempt.getSubmissions()) {
        if (submission.getQuestion() != null) {
          Question question = submission.getQuestion();

          if (isChoiceQuestionType(question.getQuestionType())) {
            totalScore += gradingService.gradeMultipleSelectionQuestion(submission);
          }
        }
      }
    }

    attempt.setScore(totalScore);
    attempt.setAiGradingSentAt(LocalDateTime.now());
    attemptRepository.save(attempt);

    clearCashes();

    try {
      aiGradingService.processAttemptForAiGrading(attempt);
      log.info("Sent expired attempt {} for AI grading", attempt.getId());
    } catch (Exception ex) {
      log.error(
          "Error sending expired attempt {} for AI grading: {}",
          attempt.getId(),
          ex.getMessage(),
          ex);
      resetAiGradingTimestamp(attempt.getId());
    }
  }

  private boolean isChoiceQuestionType(QuestionType questionType) {
    return questionType == QuestionType.MULTIPLE_CHOICE
        || questionType == QuestionType.IMAGE_WITH_MULTIPLE_CHOICE;
  }

  private Test getTestById(Long testId) {
    return testRepository
        .findById(testId)
        .orElseThrow(() -> ResourceNotFoundException.test(testId));
  }

  private Attempt getAttemptById(Long attemptId) {
    return attemptRepository
        .findByIdWithSubmissions(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));
  }

  private Question getQuestionById(Long questionId) {
    return questionRepository
        .findById(questionId)
        .orElseThrow(() -> ResourceNotFoundException.question(questionId));
  }

  private User getUserFromPrincipal(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
  }

  private void clearCashes() {
    cacheService.clearAttemptRelatedCaches();
    cacheService.clearStudentRelatedCaches();
    cacheService.clearTeacherRelatedCaches();
    cacheService.clearAdminRelatedCaches();
  }

  @Transactional(readOnly = true)
  public AttemptResultResponse completeAttemptAsync(Long attemptId) {
    Attempt gradedAttempt = getAttemptById(attemptId);
    gradedAttempt.setStatus(AttemptStatus.REVIEWED);
    gradedAttempt = attemptRepository.save(gradedAttempt);
    return dtoMapper.buildAttemptResult(gradedAttempt);
  }

  @Transactional
  public void resetAiGradingTimestamp(Long attemptId) {
    Attempt attempt = getAttemptById(attemptId);
    attempt.setAiGradingSentAt(null);
    attemptRepository.save(attempt);
  }
}
