package com.altester.core.serviceImpl.test;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.exception.*;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.Prompt;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.repository.*;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.service.TestService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.group.GroupActivityService;
import com.altester.core.serviceImpl.question.TestStatusService;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
  private final TestRepository testRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final TestDTOMapper testDTOMapper;
  private final TestAccessValidator testAccessValidator;
  private final GroupActivityService groupActivityService;
  private final NotificationDispatchService notificationService;
  private final CacheService cacheService;
  private final TestGroupSelectionService groupSelection;
  private final ApiKeyRepository apiKeyRepository;
  private final TestGroupAssignmentRepository assignmentRepository;
  private final TestRequirementsValidator testRequirementsValidator;
  private final PromptRepository promptRepository;
  private final TestStatusService testStatusService;
  private final QuestionRepository questionRepository;

  private User getCurrentUser(Principal principal) {
    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(
            () -> {
              log.error("User {} not found", principal.getName());
              return ResourceNotFoundException.user(principal.getName());
            });
  }

  private Test getTestById(Long testId) {
    return testRepository
        .findById(testId)
        .orElseThrow(
            () -> {
              log.error("Test with ID {} not found", testId);
              return ResourceNotFoundException.test(testId);
            });
  }

  private Group getGroupById(Long groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(
            () -> {
              log.error("Group with ID {} not found", groupId);
              return ResourceNotFoundException.group(
                  "To perform this action you need to specify a group id");
            });
  }

  @Override
  @Transactional
  public void toggleTeacherEditPermission(Long testId, Principal principal) {
    log.debug(
        "User {} is attempting to toggle teacher edit permission for test with ID {}",
        principal.getName(),
        testId);

    User currentUser = getCurrentUser(principal);

    if (currentUser.getRole() != RolesEnum.ADMIN) {
      log.warn("User {} is not an admin, access denied", currentUser.getUsername());
      throw AccessDeniedException.notAdmin();
    }

    Test test = getTestById(testId);
    boolean newState = !test.isAllowTeacherEdit();
    test.setAllowTeacherEdit(newState);
    testRepository.save(test);

    cacheService.clearTestRelatedCaches();

    log.info(
        "Teacher edit permission for test ID {} toggled to {} by admin {}",
        testId,
        newState,
        currentUser.getUsername());
  }

  @Override
  @Transactional
  public void toggleAiEvaluation(Long testId, Long groupId, Principal principal) {
    log.debug(
        "User {} is attempting to toggle AI evaluation for test with ID {}",
        principal.getName(),
        testId);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);
    Group group = getGroupById(groupId);

    if (currentUser.getRole() == RolesEnum.TEACHER) {
      if (group.getTeacher() == null || !group.getTeacher().getId().equals(currentUser.getId())) {
        log.warn(
            "User {} does not have access to group {}", currentUser.getUsername(), group.getId());
        throw AccessDeniedException.groupAccess();
      }
      if (!test.isAllowTeacherEdit()) {
        log.warn("Test {} does not allow teacher editing", test.getId());
        throw AccessDeniedException.testEdit();
      }
    }

    TestGroupAssignment assignment =
        assignmentRepository
            .findByTestAndGroup(test, group)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "assignment", "test and group", test.getId() + " and " + group.getId()));

    if (assignment.getApiKey() == null) {
      log.warn(
          "Cannot enable AI evaluation for test {} and group {} without an assigned API key",
          test.getId(),
          group.getId());
      throw new StateConflictException(
          "assignment", "no_api_key", "Cannot enable AI evaluation without an assigned API key");
    }

    boolean newState = !assignment.isAiEvaluation();
    assignment.setAiEvaluation(newState);
    assignmentRepository.save(assignment);

    cacheService.clearTestRelatedCaches();
    cacheService.clearApiKeyRelatedCaches();

    log.info(
        "Ai evaluation changed for test ID {} toggled to {} by user {}",
        testId,
        newState,
        currentUser.getUsername());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "tests",
      key =
          "'admin:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':active:' + "
              + "(#isActive == null ? '' : #isActive)")
  public CacheablePage<TestSummaryDTO> getAllTestsForAdmin(
      Pageable pageable, Principal principal, String searchQuery, Boolean isActive) {
    log.debug(
        "Getting all tests for admin with search query: {}, isActive: {}", searchQuery, isActive);

    User currentUser = getCurrentUser(principal);

    if (currentUser.getRole() != RolesEnum.ADMIN) {
      throw AccessDeniedException.notAdmin();
    }

    Page<Test> testsPage = testRepository.findAllWithFilters(searchQuery, isActive, pageable);

    Page<TestSummaryDTO> resultPage =
        testsPage.map(
            test -> {
              TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

              List<Group> testGroups = testDTOMapper.findGroupsByTest(test);

              List<GroupSummaryDTO> groupDTOs =
                  testGroups.stream()
                      .map(
                          group ->
                              GroupSummaryDTO.builder()
                                  .id(group.getId())
                                  .name(group.getName())
                                  .build())
                      .toList();

              dto.setAssociatedGroups(groupDTOs);
              return dto;
            });
    return new CacheablePage<>(resultPage);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "tests",
      key =
          "'teacher:' + #principal.name + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':active:' + "
              + "(#isActive == null ? '' : #isActive) + ':edit:' + "
              + "(#allowTeacherEdit == null ? '' : #allowTeacherEdit)")
  public CacheablePage<TestSummaryDTO> getTeacherTests(
      Pageable pageable,
      Principal principal,
      String searchQuery,
      Boolean isActive,
      Boolean allowTeacherEdit) {
    log.debug(
        "Getting tests for teacher with searchQuery: '{}', isActive: {}, allowTeacherEdit: {}",
        searchQuery,
        isActive,
        allowTeacherEdit);

    User currentUser = getCurrentUser(principal);

    if (currentUser.getRole() != RolesEnum.TEACHER) {
      throw AccessDeniedException.roleConflict();
    }

    Page<Test> testsPage =
        testRepository.findByTeacherWithFilters(
            currentUser.getId(), searchQuery, isActive, allowTeacherEdit, pageable);

    Page<TestSummaryDTO> resultPage =
        testsPage.map(
            test -> {
              TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

              List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
              List<Group> testGroups =
                  testDTOMapper.findGroupsByTest(test).stream()
                      .filter(teacherGroups::contains)
                      .toList();

              List<GroupSummaryDTO> groupDTOs =
                  testGroups.stream()
                      .map(
                          group ->
                              GroupSummaryDTO.builder()
                                  .id(group.getId())
                                  .name(group.getName())
                                  .build())
                      .toList();

              dto.setAssociatedGroups(groupDTOs);
              return dto;
            });
    return new CacheablePage<>(resultPage);
  }

  @Override
  @Transactional
  public TestPreviewDTO createTest(CreateTestDTO createTestDTO, Principal principal) {
    log.info("Creating new test with title: {}", createTestDTO.getTitle());

    User currentUser = getCurrentUser(principal);

    Test test = new Test();
    test.setTitle(createTestDTO.getTitle());
    test.setDescription(createTestDTO.getDescription());
    test.setDuration(createTestDTO.getDuration());
    test.setOpen(false);
    test.setMaxAttempts(createTestDTO.getMaxAttempts());

    test.setEasyQuestionsCount(
        createTestDTO.getEasyQuestionsCount() != null ? createTestDTO.getEasyQuestionsCount() : 0);
    test.setMediumQuestionsCount(
        createTestDTO.getMediumQuestionsCount() != null
            ? createTestDTO.getMediumQuestionsCount()
            : 0);
    test.setHardQuestionsCount(
        createTestDTO.getHardQuestionsCount() != null ? createTestDTO.getHardQuestionsCount() : 0);

    test.setStartTime(createTestDTO.getStartTime());
    test.setEndTime(createTestDTO.getEndTime());
    test.setCreatedByAdmin(currentUser.getRole() == RolesEnum.ADMIN);
    test.setAllowTeacherEdit(currentUser.getRole() == RolesEnum.TEACHER);

    List<Group> selectedGroups = groupSelection.findValidGroupsForTest(currentUser, createTestDTO);

    Test savedTest = testRepository.save(test);

    selectedGroups.forEach(group -> group.getTests().add(savedTest));
    groupRepository.saveAll(selectedGroups);

    List<ApiKey> globalKeys = apiKeyRepository.findAllIsGlobalTrue();
    boolean hasGlobalKey = !globalKeys.isEmpty();

    if (hasGlobalKey && !selectedGroups.isEmpty()) {
      ApiKey globalKey = globalKeys.getFirst();

      Prompt defaultPrompt = promptRepository.findById(1L).orElse(null);

      for (Group group : selectedGroups) {
        TestGroupAssignment assignment =
            TestGroupAssignment.builder()
                .test(savedTest)
                .group(group)
                .apiKey(globalKey)
                .prompt(defaultPrompt)
                .assignedAt(LocalDateTime.now())
                .assignedBy(group.getTeacher())
                .aiEvaluation(true)
                .build();

        log.debug(
            "Automatically assigning global API key {} and default prompt to test {} for group {}",
            globalKey.getId(),
            savedTest.getId(),
            group.getId());

        assignmentRepository.save(assignment);
      }
    }

    cacheService.clearAllCaches();

    return testDTOMapper.convertToTestPreviewDTO(savedTest, currentUser);
  }

  @Override
  @Transactional
  public TestPreviewDTO updateTest(CreateTestDTO updateTestDTO, Long testId, Principal principal) {
    log.info("Updating test with ID: {}", testId);

    User currentUser = getCurrentUser(principal);
    Test existingTest = getTestById(testId);

    if (currentUser.getRole() == RolesEnum.TEACHER) {
      List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
      testAccessValidator.validateTeacherEditAccess(currentUser, existingTest, teacherGroups);
    }

    Integer oldEasyScore = existingTest.getEasyQuestionScore();
    Integer oldMediumScore = existingTest.getMediumQuestionScore();
    Integer oldHardScore = existingTest.getHardQuestionScore();

    existingTest = TestUpdater.of(existingTest, updateTestDTO).updateAllFields().build();

    if (updateTestDTO.getSubjectId() != null
        || (updateTestDTO.getGroupIds() != null && !updateTestDTO.getGroupIds().isEmpty())) {

      List<Group> newSelectedGroups =
          groupSelection.findValidGroupsForTest(currentUser, updateTestDTO);

      List<Group> currentGroups = testDTOMapper.findGroupsByTest(existingTest);

      Set<Long> newGroupIds =
          newSelectedGroups.stream().map(Group::getId).collect(Collectors.toSet());

      Set<Long> currentGroupIds =
          currentGroups.stream().map(Group::getId).collect(Collectors.toSet());

      List<Group> groupsToRemove =
          currentGroups.stream().filter(group -> !newGroupIds.contains(group.getId())).toList();

      List<Group> groupsToAdd =
          newSelectedGroups.stream()
              .filter(group -> !currentGroupIds.contains(group.getId()))
              .toList();

      if (!groupsToRemove.isEmpty()) {
        Test finalExistingTest = existingTest;
        groupsToRemove.forEach(group -> group.getTests().remove(finalExistingTest));
        groupRepository.saveAll(groupsToRemove);
        log.debug("Removed test from {} groups", groupsToRemove.size());
      }

      if (!groupsToAdd.isEmpty()) {
        Test finalExistingTest1 = existingTest;
        groupsToAdd.forEach(group -> group.getTests().add(finalExistingTest1));
        groupRepository.saveAll(groupsToAdd);
        log.debug("Added test to {} new groups", groupsToAdd.size());
      }
    }

    updateQuestionScoresIfChanged(existingTest, oldEasyScore, oldMediumScore, oldHardScore);

    Test updatedTest = testRepository.save(existingTest);
    testStatusService.updateTestOpenStatus(existingTest);

    boolean parametersChanged =
        !existingTest.getTitle().equals(updateTestDTO.getTitle())
            || existingTest.getDuration() != updateTestDTO.getDuration()
            || !Objects.equals(existingTest.getStartTime(), updateTestDTO.getStartTime())
            || !Objects.equals(existingTest.getEndTime(), updateTestDTO.getEndTime());

    if (parametersChanged) {
      List<Group> affectedGroups = testDTOMapper.findGroupsByTest(existingTest);
      for (Group group : affectedGroups) {
        notificationService.notifyTestParametersChanged(existingTest, group);
      }
    }

    cacheService.clearAllCaches();

    return testDTOMapper.convertToTestPreviewDTO(updatedTest, currentUser);
  }

  private void updateQuestionScoresIfChanged(
      Test test, Integer oldEasy, Integer oldMedium, Integer oldHard) {
    Integer newEasy = test.getEasyQuestionScore();
    Integer newMedium = test.getMediumQuestionScore();
    Integer newHard = test.getHardQuestionScore();

    for (Question q : test.getQuestions()) {
      QuestionDifficulty difficulty = q.getDifficulty();

      if (difficulty == QuestionDifficulty.EASY && !Objects.equals(oldEasy, newEasy)) {
        q.setScore(newEasy);
      } else if (difficulty == QuestionDifficulty.MEDIUM && !Objects.equals(oldMedium, newMedium)) {
        q.setScore(newMedium);
      } else if (difficulty == QuestionDifficulty.HARD && !Objects.equals(oldHard, newHard)) {
        q.setScore(newHard);
      }
    }
    questionRepository.saveAll(test.getQuestions());
  }

  @Override
  @Transactional
  public void deleteTest(Long testId, Principal principal) {
    log.info("Deleting test with ID: {}", testId);

    User currentUser = getCurrentUser(principal);
    Test existingTest = getTestById(testId);

    if (currentUser.getRole() == RolesEnum.ADMIN) {
      performTestDeletion(existingTest);
    } else if (currentUser.getRole() == RolesEnum.TEACHER) {
      List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

      if (existingTest.isCreatedByAdmin()) {
        throw AccessDeniedException.testEdit();
      }

      if (!testAccessValidator.hasTestGroupAssociation(currentUser, existingTest, teacherGroups)) {
        throw AccessDeniedException.testAccess();
      }

      performTestDeletion(existingTest);
    } else {
      throw AccessDeniedException.testAccess();
    }
  }

  /**
   * Performs the actual test deletion including removing all associations with groups.
   *
   * @param test The test entity to delete
   */
  private void performTestDeletion(Test test) {
    List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
    testGroups.forEach(group -> group.getTests().remove(test));
    groupRepository.saveAll(testGroups);

    testRepository.delete(test);
    cacheService.clearAllCaches();
    log.info("Test with ID {} has been deleted", test.getId());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "testSummary", key = "'id:' + #testId")
  public TestSummaryDTO getTestSummary(Long testId, Principal principal) {
    log.debug("Getting test summary for test ID: {}", testId);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);

    testAccessValidator.validateTestAccess(currentUser, test);

    TestSummaryDTO summaryDTO = testDTOMapper.convertToTestSummaryDTO(test);

    List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
    List<GroupSummaryDTO> groupDTOs = new ArrayList<>();

    if (currentUser.getRole() == RolesEnum.ADMIN) {
      for (Group group : testGroups) {
        groupDTOs.add(GroupSummaryDTO.builder().id(group.getId()).name(group.getName()).build());
      }
    } else if (currentUser.getRole() == RolesEnum.TEACHER) {
      List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
      for (Group group : testGroups) {
        if (teacherGroups.contains(group)) {
          groupDTOs.add(GroupSummaryDTO.builder().id(group.getId()).name(group.getName()).build());
        }
      }
    }

    summaryDTO.setAssociatedGroups(groupDTOs);
    return summaryDTO;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "test", key = "'id:' + #testId")
  public TestPreviewDTO getTestPreview(Long testId, Principal principal) {
    log.debug("Getting test preview for test ID: {}", testId);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);

    testAccessValidator.validateTestAccess(currentUser, test);

    return testDTOMapper.convertToTestPreviewDTO(test, currentUser);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "testsBySubject",
      key =
          "'subject:' + #subjectId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':active:' + "
              + "(#isActive == null ? '' : #isActive)")
  public CacheablePage<TestSummaryDTO> getTestsBySubject(
      Long subjectId,
      Principal principal,
      String searchQuery,
      Boolean isActive,
      Pageable pageable) {
    log.debug(
        "Getting tests for subject ID: {} with search query: {}, isActive: {}",
        subjectId,
        searchQuery,
        isActive);

    User currentUser = getCurrentUser(principal);

    Subject subject =
        subjectRepository
            .findById(subjectId)
            .orElseThrow(() -> ResourceNotFoundException.subject(subjectId));

    Page<Test> testsPage =
        testRepository.findBySubjectWithFilters(subjectId, searchQuery, isActive, pageable);

    List<Group> teacherGroups =
        currentUser.getRole() == RolesEnum.TEACHER
            ? groupRepository.findByTeacher(currentUser)
            : Collections.emptyList();

    Page<TestSummaryDTO> resultPage =
        testsPage.map(
            test -> {
              TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

              List<Group> testGroups =
                  testDTOMapper.findGroupsByTest(test).stream()
                      .filter(group -> subject.getGroups().contains(group))
                      .filter(
                          group ->
                              currentUser.getRole() == RolesEnum.ADMIN
                                  || (currentUser.getRole() == RolesEnum.TEACHER
                                      && teacherGroups.contains(group)))
                      .toList();

              List<GroupSummaryDTO> groupDTOs =
                  testGroups.stream()
                      .map(
                          group ->
                              GroupSummaryDTO.builder()
                                  .id(group.getId())
                                  .name(group.getName())
                                  .build())
                      .toList();

              dto.setAssociatedGroups(groupDTOs);
              return dto;
            });

    return new CacheablePage<>(resultPage);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "testsByGroup",
      key =
          "'group:' + #groupId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':search:' + "
              + "(#searchQuery == null ? '' : #searchQuery) + ':active:' + "
              + "(#isActive == null ? '' : #isActive)")
  public CacheablePage<TestSummaryDTO> getTestsByGroup(
      Long groupId, Principal principal, String searchQuery, Boolean isActive, Pageable pageable) {
    log.debug(
        "Getting tests for group ID: {} with search query: {}, isActive: {}",
        groupId,
        searchQuery,
        isActive);

    User currentUser = getCurrentUser(principal);

    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> ResourceNotFoundException.group(groupId));

    testAccessValidator.validateGroupAccess(currentUser, group);

    Page<Test> testsPage =
        testRepository.findByGroupWithFilters(groupId, searchQuery, isActive, pageable);

    Page<TestSummaryDTO> resultPage =
        testsPage.map(
            test -> {
              TestSummaryDTO dto = testDTOMapper.convertToTestSummaryDTO(test);

              List<GroupSummaryDTO> groupDTOs = new ArrayList<>();
              groupDTOs.add(
                  GroupSummaryDTO.builder().id(group.getId()).name(group.getName()).build());

              dto.setAssociatedGroups(groupDTOs);
              return dto;
            });
    return new CacheablePage<>(resultPage);
  }

  @Override
  @Transactional
  public void toggleTestActivity(Long testId, Principal principal) {
    log.info(
        "User {} is attempting to toggle activity for test with ID {}",
        principal.getName(),
        testId);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);

    if (currentUser.getRole() != RolesEnum.ADMIN) {
      log.info("User {} is not an admin, checking permissions...", currentUser.getUsername());

      List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
      testAccessValidator.validateTeacherEditAccess(currentUser, test, teacherGroups);
    }

    boolean newState = !test.isOpen();

    if (newState && !testRequirementsValidator.requirementsMet(test)) {
      String errorMessage = testRequirementsValidator.getMissingRequirements(test);
      log.warn("Cannot open test ID {}: {}", testId, errorMessage);
      throw new StateConflictException("test", "requirements_not_met", errorMessage);
    }

    test.setOpen(newState);
    testRepository.save(test);

    cacheService.clearTestRelatedCaches();
    cacheService.clearStudentRelatedCaches();
    cacheService.clearTeacherRelatedCaches();

    if (Boolean.TRUE.equals(newState)) {
      List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
      for (Group group : testGroups) {
        notificationService.notifyTestAssigned(test, group);
      }
    }

    log.info(
        "Test ID {} activity toggled to {} by user {}",
        testId,
        newState,
        currentUser.getUsername());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "testQuestions",
      key =
          "'testId:' + #testId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':difficulty:' + (#difficulty != null ? #difficulty : 'ALL')")
  public CacheablePage<QuestionDTO> getTestQuestions(
      Long testId, Principal principal, Pageable pageable, QuestionDifficulty difficulty) {
    log.debug("Getting questions for test ID: {} with difficulty: {}", testId, difficulty);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);

    testAccessValidator.validateTestAccess(currentUser, test);

    List<Question> filteredQuestions = new ArrayList<>(test.getQuestions());

    if (difficulty != null) {
      filteredQuestions =
          filteredQuestions.stream().filter(q -> q.getDifficulty() == difficulty).toList();
    } else {
      filteredQuestions.sort(Comparator.comparing(Question::getDifficulty).reversed());
    }

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());

    if (start > filteredQuestions.size()) {
      return new CacheablePage<>(Page.empty());
    }

    List<Question> pagedQuestions = filteredQuestions.subList(start, end);
    List<QuestionDTO> questionDTOS =
        pagedQuestions.stream().map(testDTOMapper::convertToQuestionDTO).toList();

    Page<QuestionDTO> result = new PageImpl<>(questionDTOS, pageable, filteredQuestions.size());

    return new CacheablePage<>(result);
  }

  @Override
  @Transactional(readOnly = true)
  public CacheablePage<QuestionDTO> getStudentTestPreview(
      Long testId, Principal principal, Pageable pageable) {
    log.debug("Getting student test preview for test ID: {}", testId);

    User currentUser = getCurrentUser(principal);
    Test test = getTestById(testId);

    testAccessValidator.validateTestAccess(currentUser, test);

    Map<QuestionDifficulty, List<Question>> questionsByDifficulty =
        test.getQuestions().stream().collect(Collectors.groupingBy(Question::getDifficulty));

    List<Question> selectedQuestions = new ArrayList<>();

    if (test.getEasyQuestionsCount() > 0) {
      List<Question> easyQuestions =
          questionsByDifficulty.getOrDefault(QuestionDifficulty.EASY, Collections.emptyList());
      selectedQuestions.addAll(selectRandomQuestions(easyQuestions, test.getEasyQuestionsCount()));
    }

    if (test.getMediumQuestionsCount() > 0) {
      List<Question> mediumQuestions =
          questionsByDifficulty.getOrDefault(QuestionDifficulty.MEDIUM, Collections.emptyList());
      selectedQuestions.addAll(
          selectRandomQuestions(mediumQuestions, test.getMediumQuestionsCount()));
    }

    if (test.getHardQuestionsCount() > 0) {
      List<Question> hardQuestions =
          questionsByDifficulty.getOrDefault(QuestionDifficulty.HARD, Collections.emptyList());
      selectedQuestions.addAll(selectRandomQuestions(hardQuestions, test.getHardQuestionsCount()));
    }

    Collections.shuffle(selectedQuestions);

    List<QuestionDTO> questionDTOs =
        selectedQuestions.stream().map(testDTOMapper::convertToQuestionDTO).toList();

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), questionDTOs.size());

    if (start > questionDTOs.size()) {
      return new CacheablePage<>(Page.empty());
    }

    List<QuestionDTO> pagedQuestionDTOS = questionDTOs.subList(start, end);

    Page<QuestionDTO> result = new PageImpl<>(pagedQuestionDTOS, pageable, questionDTOs.size());

    return new CacheablePage<>(result);
  }

  private List<Question> selectRandomQuestions(List<Question> questions, int count) {
    if (questions.size() <= count) {
      return new ArrayList<>(questions);
    }

    List<Question> questionsCopy = new ArrayList<>(questions);
    Collections.shuffle(questionsCopy);
    return questionsCopy.subList(0, count);
  }
}
