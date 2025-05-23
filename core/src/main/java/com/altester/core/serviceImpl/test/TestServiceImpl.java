package com.altester.core.serviceImpl.test;

import com.altester.core.dtos.core_service.test.*;
import com.altester.core.exception.*;
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
import com.altester.core.util.CacheablePage;
import java.security.Principal;
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
  private final ApiKeyRepository apiKeyRepository;
  private final TestGroupAssignmentRepository assignmentRepository;
  private final PromptRepository promptRepository;
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
  @Cacheable(value = "test", key = "'id:' + #testId + ':user:' + #principal.name")
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
