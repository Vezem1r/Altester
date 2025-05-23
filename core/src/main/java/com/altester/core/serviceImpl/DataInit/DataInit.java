package com.altester.core.serviceImpl.DataInit;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.Prompt;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.ApiKey.enums.AiServiceName;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.*;
import com.altester.core.model.subject.enums.*;
import com.altester.core.repository.*;
import com.altester.core.serviceImpl.group.GroupActivityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataInit {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SubjectRepository subjectRepository;
  private final GroupRepository groupRepository;
  private final TestRepository testRepository;
  private final QuestionRepository questionRepository;
  private final OptionRepository optionRepository;
  private final SemesterConfig semesterConfig;
  private final GroupActivityService groupActivityService;
  private final PromptRepository promptRepository;
  private final ApiKeyRepository apiKeyRepository;
  private final AttemptRepository attemptRepository;
  private final SubmissionRepository submissionRepository;
  private final TestGroupAssignmentRepository testGroupAssignmentRepository;

  @PersistenceContext private EntityManager entityManager;

  private final Random random = new Random();

  @Value("${admin.password}")
  private String adminPassword;

  @Value("${default.password}")
  private String defaultPassword;

  @Value("${test.student.password}")
  private String testStudentPassword;

  @Value("${test.teacher.password}")
  private String testTeacherPassword;

  private User baseAdmin;
  private User baseStudent;
  private User baseTeacher;

  public boolean isDataAlreadyInitialized() {
    long userCount = userRepository.count();
    long subjectCount = subjectRepository.count();
    long groupCount = groupRepository.count();
    return userCount > 10 && subjectCount > 5 && groupCount > 10;
  }

  @Transactional
  public void createDefaultAdmin() {
    log.info("Phase 1: Creating default admin user");

    if (userRepository.findByUsername(DataConstants.ADMIN_USERNAME).isEmpty()) {
      String encodedAdminPassword = passwordEncoder.encode(adminPassword);
      baseAdmin =
          User.builder()
              .name("Admin")
              .surname(DataConstants.SURNAME_SUPER)
              .email("admin@vsb.cz")
              .username(DataConstants.ADMIN_USERNAME)
              .created(LocalDateTime.now())
              .lastLogin(LocalDateTime.now())
              .password(encodedAdminPassword)
              .enabled(true)
              .isRegistered(true)
              .role(RolesEnum.ADMIN)
              .build();
      baseAdmin = userRepository.save(baseAdmin);
      log.info("Created admin user: {}", baseAdmin.getUsername());
    } else {
      baseAdmin = userRepository.findByUsername(DataConstants.ADMIN_USERNAME).get();
      log.info("Admin user already exists: {}", baseAdmin.getUsername());
    }
  }

  @Transactional
  public void createBaseUsers() {
    log.info("Phase 2: Creating base student and teacher users");

    if (userRepository.findByUsername(DataConstants.STUDENT_USERNAME).isEmpty()) {
      String encodedStudentPassword = passwordEncoder.encode(testStudentPassword);
      baseStudent =
          User.builder()
              .name("Student")
              .surname(DataConstants.SURNAME_SUPER)
              .email("student@vsb.cz")
              .username(DataConstants.STUDENT_USERNAME)
              .created(LocalDateTime.now())
              .lastLogin(LocalDateTime.now())
              .password(encodedStudentPassword)
              .enabled(true)
              .isRegistered(true)
              .role(RolesEnum.STUDENT)
              .build();
      baseStudent = userRepository.save(baseStudent);
      log.info("Created base student user: {}", baseStudent.getUsername());
    } else {
      baseStudent = userRepository.findByUsername(DataConstants.STUDENT_USERNAME).get();
      log.info("Base student already exists: {}", baseStudent.getUsername());
    }

    if (userRepository.findByUsername(DataConstants.TEACHER_USERNAME).isEmpty()) {
      String encodedTeacherPassword = passwordEncoder.encode(testTeacherPassword);
      baseTeacher =
          User.builder()
              .name("Teacher")
              .surname(DataConstants.SURNAME_SUPER)
              .email("teacher@vsb.cz")
              .username(DataConstants.TEACHER_USERNAME)
              .created(LocalDateTime.now())
              .lastLogin(LocalDateTime.now())
              .password(encodedTeacherPassword)
              .enabled(true)
              .isRegistered(true)
              .role(RolesEnum.TEACHER)
              .build();
      baseTeacher = userRepository.save(baseTeacher);
      log.info("Created base teacher user: {}", baseTeacher.getUsername());
    } else {
      baseTeacher = userRepository.findByUsername(DataConstants.TEACHER_USERNAME).get();
      log.info("Base teacher already exists: {}", baseTeacher.getUsername());
    }
  }

  @Transactional
  public void createStudents(int amount) {
    log.info("Phase 3: Creating {} students", amount);

    Pageable pageable = PageRequest.of(0, 300);
    var studentPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);
    int currentCount = (int) studentPage.getTotalElements();

    int existingBulkStudents = Math.max(0, currentCount - 1);
    int remainingToCreate = Math.max(0, amount - existingBulkStudents);

    if (remainingToCreate == 0) {
      log.info("Already have {} bulk students, skipping student creation", existingBulkStudents);
      return;
    }

    log.info("Creating {} additional students", remainingToCreate);

    for (int i = 0; i < remainingToCreate && i < DataConstants.SURNAMES.size(); i++) {
      String firstname = DataConstants.FIRSTNAMES.get(i % DataConstants.FIRSTNAMES.size());
      String lastname = DataConstants.SURNAMES.get(i);
      String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + ".st@vsb.cz";

      if (userRepository.findByEmail(email).isPresent()) {
        log.debug("Skipping existing student with email: {}", email);
        continue;
      }

      String username = generateUsername(lastname);
      String pass = passwordEncoder.encode(defaultPassword);

      User user =
          User.builder()
              .name(firstname)
              .surname(lastname)
              .email(email)
              .username(username)
              .created(LocalDateTime.now())
              .lastLogin(LocalDateTime.now().minusDays(random.nextInt(30)))
              .password(pass)
              .enabled(true)
              .isRegistered(true)
              .role(RolesEnum.STUDENT)
              .build();

      userRepository.save(user);
      log.debug("Created student: {} {}", firstname, lastname);
    }
  }

  @Transactional
  public void createTeachers(int amount) {
    log.info("Phase 3: Creating {} teachers", amount);

    Pageable pageable = PageRequest.of(0, 100);
    var teacherPage = userRepository.findByRole(RolesEnum.TEACHER, pageable);
    int currentCount = (int) teacherPage.getTotalElements();

    int existingBulkTeachers = Math.max(0, currentCount - 1);
    int remainingToCreate = Math.max(0, amount - existingBulkTeachers);

    if (remainingToCreate == 0) {
      log.info("Already have {} bulk teachers, skipping teacher creation", existingBulkTeachers);
      return;
    }

    log.info("Creating {} additional teachers", remainingToCreate);

    for (int i = 0; i < remainingToCreate && i < DataConstants.SURNAMES.size(); i++) {
      String firstname = DataConstants.FIRSTNAMES.get((i + 30) % DataConstants.FIRSTNAMES.size());
      String lastname = DataConstants.SURNAMES.get((i + 30) % DataConstants.SURNAMES.size());
      String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@vsb.cz";

      if (userRepository.findByEmail(email).isPresent()) {
        log.debug("Skipping existing teacher with email: {}", email);
        continue;
      }

      String username = generateUsername(lastname);
      String pass = passwordEncoder.encode(defaultPassword);

      User user =
          User.builder()
              .name(firstname)
              .surname(lastname)
              .email(email)
              .username(username)
              .created(LocalDateTime.now())
              .lastLogin(LocalDateTime.now().minusDays(random.nextInt(14)))
              .password(pass)
              .enabled(true)
              .isRegistered(true)
              .role(RolesEnum.TEACHER)
              .build();

      userRepository.save(user);
      log.debug("Created teacher: {} {}", firstname, lastname);
    }
  }

  @Transactional
  public void createITSubjects() {
    log.info("Phase 4: Creating IT subjects");

    for (Map<String, String> subjectData : DataConstants.IT_SUBJECTS) {
      String shortName = subjectData.get(DataConstants.KEY_SHORT_NAME);

      if (subjectRepository.findByShortName(shortName).isPresent()) {
        log.debug("Subject {} already exists, skipping", shortName);
        continue;
      }

      Subject subject =
          Subject.builder()
              .name(subjectData.get(DataConstants.KEY_NAME))
              .shortName(shortName)
              .description(subjectData.get(DataConstants.KEY_DESCRIPTION))
              .modified(LocalDateTime.now())
              .groups(new HashSet<>())
              .build();

      subjectRepository.save(subject);
      log.info("Created subject: {} ({})", subject.getName(), subject.getShortName());
    }
  }

  @Transactional
  public void createGroupsForAllSubjects() {
    log.info("Phase 5: Creating groups for all subjects");

    List<Subject> subjects = subjectRepository.findAll();
    List<User> teachers = userRepository.findAllByRole(RolesEnum.TEACHER);
    List<User> students = userRepository.findAllByRole(RolesEnum.STUDENT);

    if (teachers.isEmpty() || students.isEmpty()) {
      log.error("No teachers or students available to create groups");
      return;
    }

    Semester currentSemester = semesterConfig.getCurrentSemester();
    int currentYear = semesterConfig.getCurrentAcademicYear();

    for (Subject subject : subjects) {
      for (int i = 0; i < 2; i++) {
        String groupName =
            generateGroupName(subject.getShortName(), currentSemester, currentYear, i);

        if (groupRepository.findByName(groupName).isPresent()) {
          log.debug("Group {} already exists, skipping", groupName);
          continue;
        }

        User teacher = teachers.get(random.nextInt(teachers.size()));
        while (teacher.equals(baseTeacher) && teachers.size() > 1) {
          teacher = teachers.get(random.nextInt(teachers.size()));
        }

        Group group = createGroup(groupName, subject, teacher, currentSemester, currentYear, true);

        addStudentsToGroup(
            group, students, subject, currentSemester, currentYear, 15 + random.nextInt(11));

        log.info("Created group {} for subject {}", groupName, subject.getShortName());
      }
    }
  }

  @Transactional
  public void createSpecialGroupsForBaseTeacher() {
    log.info("Phase 6: Creating special groups for base teacher");

    List<Subject> subjects = subjectRepository.findAll();
    if (subjects.size() < 2) {
      log.error("Not enough subjects to create special groups");
      return;
    }

    Subject subject1 = subjects.get(0);
    Subject subject2 = subjects.get(1);

    Semester currentSemester = semesterConfig.getCurrentSemester();
    int currentYear = semesterConfig.getCurrentAcademicYear();
    Semester nextSemester =
        (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
    int nextYear = (nextSemester == Semester.WINTER) ? currentYear + 1 : currentYear;
    Semester prevSemester =
        (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
    int prevYear = (prevSemester == Semester.SUMMER) ? currentYear - 1 : currentYear;

    List<User> students = userRepository.findAllByRole(RolesEnum.STUDENT);

    createTeacherGroup(subject1, baseTeacher, students, currentSemester, currentYear, 0, true);
    createTeacherGroup(subject1, baseTeacher, students, prevSemester, prevYear, 1, false);
    createTeacherGroup(subject1, baseTeacher, students, nextSemester, nextYear, 2, false);
    createTeacherGroup(subject1, baseTeacher, students, nextSemester, nextYear, 3, false);

    createTeacherGroup(subject2, baseTeacher, students, currentSemester, currentYear, 0, true);
    createTeacherGroup(subject2, baseTeacher, students, prevSemester, prevYear, 1, false);
    createTeacherGroup(subject2, baseTeacher, students, nextSemester, nextYear, 2, false);
    createTeacherGroup(subject2, baseTeacher, students, nextSemester, nextYear, 3, false);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createTeacherGroup(
      Subject subject,
      User teacher,
      List<User> students,
      Semester semester,
      int year,
      int index,
      boolean isActive) {
    String groupName = "T-" + generateGroupName(subject.getShortName(), semester, year, index);

    if (groupRepository.findByName(groupName).isPresent()) {
      log.debug("Teacher group {} already exists, skipping", groupName);
      return;
    }

    Group group = createGroup(groupName, subject, teacher, semester, year, isActive);

    Set<User> groupStudents = new HashSet<>();
    groupStudents.add(baseStudent);

    int studentCount = 15 + random.nextInt(6);
    for (int i = 0; i < studentCount && i < students.size(); i++) {
      User student = students.get(random.nextInt(students.size()));
      if (!student.equals(baseStudent)) {
        groupStudents.add(student);
      }
    }

    group.setStudents(groupStudents);
    groupRepository.save(group);

    log.info(
        "Created special teacher group {} for subject {} ({}/{}) - {}",
        groupName,
        subject.getShortName(),
        semester,
        year,
        isActive ? DataConstants.GROUP_ACTIVE : "inactive");
  }

  @Transactional
  public void createPrompts() {
    log.info("Phase 7: Creating prompts");

    for (int i = 0; i < 3; i++) {
      String title = "Admin Global Prompt " + (i + 1);
      if (promptRepository.findByTitle(title).isPresent()) {
        log.debug("Prompt {} already exists, skipping", title);
        continue;
      }

      Prompt prompt =
          Prompt.builder()
              .title(title)
              .description("Global grading prompt created by admin for all teachers to use")
              .prompt(DataConstants.PROMPT_TEMPLATES.get(i % DataConstants.PROMPT_TEMPLATES.size()))
              .author(baseAdmin)
              .isPublic(true)
              .created(LocalDateTime.now())
              .build();

      promptRepository.save(prompt);
      log.info("Created global prompt: {}", title);
    }

    for (int i = 0; i < 3; i++) {
      String title = "Teacher Prompt " + (i + 1);
      if (promptRepository.findByTitle(title).isPresent()) {
        log.debug("Prompt {} already exists, skipping", title);
        continue;
      }

      Prompt prompt =
          Prompt.builder()
              .title(title)
              .description("Custom grading prompt created by base teacher")
              .prompt(
                  DataConstants.PROMPT_TEMPLATES.get(
                      (i + 3) % DataConstants.PROMPT_TEMPLATES.size()))
              .author(baseTeacher)
              .isPublic(i == 0)
              .created(LocalDateTime.now())
              .build();

      promptRepository.save(prompt);
      log.info("Created teacher prompt: {} (public: {})", title, i == 0);
    }
  }

  @Transactional
  public void createApiKeys() {
    log.info("Phase 8: Creating API keys");

    createApiKey("Admin Global API Key 1", AiServiceName.OPENAI, "gpt-4", baseAdmin, true);
    createApiKey(
        "Admin Global API Key 2",
        AiServiceName.ANTHROPIC_CLAUDE,
        "claude-3-opus-20240229",
        baseAdmin,
        true);

    createApiKey("Teacher API Key 1", AiServiceName.OPENAI, "gpt-3.5-turbo", baseTeacher, false);
    createApiKey("Teacher API Key 2", AiServiceName.GEMINI, "gemini-pro", baseTeacher, false);
    createApiKey(
        "Teacher API Key 3",
        AiServiceName.ANTHROPIC_CLAUDE,
        "claude-3-sonnet-20240229",
        baseTeacher,
        false);
  }

  private void createApiKey(
      String name, AiServiceName service, String model, User owner, boolean isGlobal) {
    if (apiKeyRepository.findByName(name).isPresent()) {
      log.debug("API key {} already exists, skipping", name);
      return;
    }

    String keyPrefix = generateRandomString(10);
    String keySuffix = generateRandomString(10);

    ApiKey apiKey =
        ApiKey.builder()
            .name(name)
            .encryptedKey(passwordEncoder.encode(keyPrefix + "..." + keySuffix))
            .keyPrefix(keyPrefix)
            .keySuffix(keySuffix)
            .aiServiceName(service)
            .model(model)
            .isGlobal(isGlobal)
            .owner(owner)
            .createdAt(LocalDateTime.now())
            .description("API key for " + service)
            .isActive(true)
            .build();

    apiKeyRepository.save(apiKey);
    log.info("Created API key: {} for {} (global: {})", name, service, isGlobal);
  }

  @Transactional
  public void createTestsForAllGroups() {
    log.info("Phase 9: Creating tests for all groups");

    List<Group> groups = groupRepository.findAll();
    List<ApiKey> apiKeys = apiKeyRepository.findAll();
    List<Prompt> prompts = promptRepository.findAll();

    for (Group group : groups) {
      Subject subject = getSubjectForGroup(group);
      if (subject == null) continue;

      for (int i = 0; i < 2; i++) {
        String testTitle = subject.getShortName() + " - Test " + (i + 1);

        boolean isOpenQuestions = i % 2 == 0;
        Test test = createTest(testTitle, group);

        createQuestionsForTest(test, subject, isOpenQuestions);

        if (group.isActive() && random.nextDouble() < 0.5) {
          assignApiKeyAndPromptToTest(test, group, apiKeys, prompts);
        }

        log.info(
            "Created test '{}' for group {} (open questions: {})",
            testTitle,
            group.getName(),
            isOpenQuestions);
      }
    }
  }

  @Transactional
  public void createAttemptsAndSubmissions() {
    log.info("Phase 10: Creating attempts and submissions");

    List<Test> tests = testRepository.findAll();

    for (Test test : tests) {
      Set<Group> testGroups = getGroupsForTest(test);

      for (Group group : testGroups) {
        if (!group.isActive()) continue;

        Set<User> students = group.getStudents();
        for (User student : students) {
          createAttemptWithSubmissions(test, student);
        }
      }
    }
  }

  private Group createGroup(
      String name, Subject subject, User teacher, Semester semester, int year, boolean active) {
    Group group =
        Group.builder()
            .name(name)
            .teacher(teacher)
            .students(new HashSet<>())
            .semester(semester)
            .academicYear(year)
            .active(active)
            .tests(new HashSet<>())
            .build();

    Group savedGroup = groupRepository.save(group);

    subject.getGroups().add(savedGroup);
    subjectRepository.save(subject);

    return savedGroup;
  }

  private void addStudentsToGroup(
      Group group,
      List<User> allStudents,
      Subject subject,
      Semester semester,
      int year,
      int count) {
    Set<User> groupStudents = new HashSet<>();

    List<User> availableStudents =
        allStudents.stream()
            .filter(s -> !s.equals(baseStudent) && !s.equals(baseTeacher) && !s.equals(baseAdmin))
            .filter(s -> !isStudentInSubjectGroup(s, subject, semester, year))
            .collect(Collectors.toList());

    Collections.shuffle(availableStudents);
    for (int i = 0; i < count && i < availableStudents.size(); i++) {
      groupStudents.add(availableStudents.get(i));
    }

    group.setStudents(groupStudents);
    groupRepository.save(group);
  }

  private boolean isStudentInSubjectGroup(
      User student, Subject subject, Semester semester, int year) {
    List<Group> subjectGroups =
        groupRepository.findAll().stream()
            .filter(g -> g.getSemester() == semester && g.getAcademicYear() == year)
            .filter(g -> g.getStudents().contains(student))
            .filter(
                g -> {
                  Subject groupSubject = getSubjectForGroup(g);
                  return groupSubject != null && groupSubject.equals(subject);
                })
            .toList();

    return !subjectGroups.isEmpty();
  }

  private Subject getSubjectForGroup(Group group) {
    return subjectRepository.findAll().stream()
        .filter(s -> s.getGroups().contains(group))
        .findFirst()
        .orElse(null);
  }

  private Set<Group> getGroupsForTest(Test test) {
    return groupRepository.findAll().stream()
        .filter(g -> g.getTests().contains(test))
        .collect(Collectors.toSet());
  }

  private Test createTest(String title, Group group) {
    LocalDateTime startTime = LocalDateTime.now().minusDays(7);
    LocalDateTime endTime = LocalDateTime.now().plusDays(7);

    if (!group.isActive()) {
      startTime = LocalDateTime.now().minusMonths(3);
      endTime = startTime.plusDays(14);
    }

    int duration = 60 + random.nextInt(61);
    int maxAttempts = 1 + random.nextInt(3);

    int easyCount = 2 + random.nextInt(3);
    int mediumCount = 2 + random.nextInt(3);
    int hardCount = 1 + random.nextInt(2);

    Test test =
        Test.builder()
            .title(title)
            .description("Assessment for " + title)
            .duration(duration)
            .isOpen(group.isActive())
            .maxAttempts(maxAttempts)
            .easyQuestionsCount(easyCount)
            .mediumQuestionsCount(mediumCount)
            .hardQuestionsCount(hardCount)
            .easyQuestionScore(10)
            .mediumQuestionScore(20)
            .hardQuestionScore(30)
            .startTime(startTime)
            .endTime(endTime)
            .isCreatedByAdmin(random.nextBoolean())
            .allowTeacherEdit(true)
            .build();

    Test savedTest = testRepository.save(test);

    group.getTests().add(savedTest);
    groupRepository.save(group);

    return savedTest;
  }

  private void createQuestionsForTest(Test test, Subject subject, boolean isOpenQuestions) {
    QuestionType questionType =
        isOpenQuestions ? QuestionType.TEXT_ONLY : QuestionType.MULTIPLE_CHOICE;

    createQuestionsOfDifficulty(
        test,
        subject,
        test.getEasyQuestionsCount() * 2,
        QuestionDifficulty.EASY,
        test.getEasyQuestionScore(),
        questionType);
    createQuestionsOfDifficulty(
        test,
        subject,
        test.getMediumQuestionsCount() * 2,
        QuestionDifficulty.MEDIUM,
        test.getMediumQuestionScore(),
        questionType);
    createQuestionsOfDifficulty(
        test,
        subject,
        test.getHardQuestionsCount() * 2,
        QuestionDifficulty.HARD,
        test.getHardQuestionScore(),
        questionType);
  }

  private void createQuestionsOfDifficulty(
      Test test,
      Subject subject,
      int count,
      QuestionDifficulty difficulty,
      int score,
      QuestionType type) {
    for (int i = 0; i < count; i++) {
      String questionText = generateQuestionText(subject, difficulty, i);
      String correctAnswer = "Model answer for: " + questionText;

      Question question =
          Question.builder()
              .questionText(questionText)
              .score(score)
              .correctAnswer(correctAnswer)
              .questionType(type)
              .difficulty(difficulty)
              .test(test)
              .build();

      Question savedQuestion = questionRepository.save(question);

      if (type == QuestionType.MULTIPLE_CHOICE) {
        createOptionsForQuestion(savedQuestion, difficulty);
      }
    }
  }

  private String generateQuestionText(Subject subject, QuestionDifficulty difficulty, int index) {
    String prefix =
        switch (difficulty) {
          case EASY -> "Define";
          case MEDIUM -> "Explain";
          case HARD -> "Analyze and discuss";
        };

    String topic = subject.getName().toLowerCase().replace(" ", "_") + "_concept_" + index;
    return prefix + " the " + topic + " in the context of " + subject.getName();
  }

  private void createOptionsForQuestion(Question question, QuestionDifficulty difficulty) {
    int optionCount =
        switch (difficulty) {
          case EASY -> 3;
          case MEDIUM -> 4;
          case HARD -> 5;
        };

    for (int i = 0; i < optionCount; i++) {
      boolean isCorrect = i == 0;

      Option option =
          Option.builder()
              .text(
                  "Option "
                      + (i + 1)
                      + " for question: "
                      + question.getQuestionText().substring(0, 30)
                      + "...")
              .description("")
              .isCorrect(isCorrect)
              .question(question)
              .build();

      optionRepository.save(option);
    }
  }

  private void createAttemptWithSubmissions(Test test, User student) {
    boolean hasAiGrading =
        testGroupAssignmentRepository.findAll().stream()
            .anyMatch(tga -> tga.getTest().equals(test) && tga.isAiEvaluation());

    LocalDateTime startTime = test.getStartTime().plusHours(random.nextInt(24));
    LocalDateTime endTime = startTime.plusMinutes(random.nextInt(test.getDuration()));

    AttemptStatus status = hasAiGrading ? AttemptStatus.AI_REVIEWED : AttemptStatus.REVIEWED;

    Attempt attempt =
        Attempt.builder()
            .attemptNumber(1)
            .startTime(startTime)
            .endTime(endTime)
            .status(status)
            .test(test)
            .student(student)
            .aiGradingSentAt(hasAiGrading ? endTime.plusMinutes(5) : null)
            .build();

    Attempt savedAttempt = attemptRepository.save(attempt);

    List<Question> selectedQuestions = selectQuestionsForAttempt(test);
    int totalScore = 0;
    int aiTotalScore = 0;

    for (int i = 0; i < selectedQuestions.size(); i++) {
      Question question = selectedQuestions.get(i);

      int maxScore = question.getScore();
      int score = (int) (maxScore * (0.5 + random.nextDouble() * 0.5));
      int aiScore = (int) (maxScore * (0.4 + random.nextDouble() * 0.6));

      totalScore += score;
      aiTotalScore += aiScore;

      Submission submission =
          Submission.builder()
              .answerText("Student answer for: " + question.getQuestionText())
              .score(score)
              .aiScore(aiScore)
              .attempt(savedAttempt)
              .question(question)
              .orderIndex(i)
              .aiGraded(hasAiGrading)
              .aiFeedback(hasAiGrading ? "AI feedback: Good understanding shown" : null)
              .teacherFeedback("Teacher feedback: Well done")
              .build();

      submissionRepository.save(submission);
    }

    savedAttempt.setScore(totalScore);
    savedAttempt.setAiScore(aiTotalScore);
    attemptRepository.save(savedAttempt);
  }

  private List<Question> selectQuestionsForAttempt(Test test) {
    List<Question> allQuestions = questionRepository.findByTest(test);

    List<Question> easyQuestions =
        allQuestions.stream()
            .filter(q -> q.getDifficulty() == QuestionDifficulty.EASY)
            .collect(Collectors.toList());

    List<Question> mediumQuestions =
        allQuestions.stream()
            .filter(q -> q.getDifficulty() == QuestionDifficulty.MEDIUM)
            .collect(Collectors.toList());

    List<Question> hardQuestions =
        allQuestions.stream()
            .filter(q -> q.getDifficulty() == QuestionDifficulty.HARD)
            .collect(Collectors.toList());

    List<Question> selectedQuestions = new ArrayList<>();

    Collections.shuffle(easyQuestions);
    selectedQuestions.addAll(easyQuestions.stream().limit(test.getEasyQuestionsCount()).toList());

    Collections.shuffle(mediumQuestions);
    selectedQuestions.addAll(
        mediumQuestions.stream().limit(test.getMediumQuestionsCount()).toList());

    Collections.shuffle(hardQuestions);
    selectedQuestions.addAll(hardQuestions.stream().limit(test.getHardQuestionsCount()).toList());

    return selectedQuestions;
  }

  private void assignApiKeyAndPromptToTest(
      Test test, Group group, List<ApiKey> apiKeys, List<Prompt> prompts) {
    if (apiKeys.isEmpty() || prompts.isEmpty()) return;

    ApiKey apiKey = apiKeys.get(random.nextInt(apiKeys.size()));
    Prompt prompt = prompts.get(random.nextInt(prompts.size()));

    TestGroupAssignment assignment =
        TestGroupAssignment.builder()
            .test(test)
            .group(group)
            .apiKey(apiKey)
            .prompt(prompt)
            .assignedAt(LocalDateTime.now())
            .assignedBy(group.getTeacher())
            .aiEvaluation(true)
            .build();

    testGroupAssignmentRepository.save(assignment);
    log.debug(
        "Assigned API key and prompt to test '{}' for group '{}'",
        test.getTitle(),
        group.getName());
  }

  private String generateGroupName(String subjectCode, Semester semester, int year, int index) {
    String semesterCode = semester == Semester.WINTER ? "W" : "S";
    return String.format("%s-%s%d-%d", subjectCode, semesterCode, year % 100, index + 1);
  }

  private String generateUsername(String surname) {
    String prefix = surname.substring(0, Math.min(3, surname.length())).toUpperCase();
    String username;
    boolean isUnique;

    do {
      int randomNumber = 100 + random.nextInt(900);
      username = String.format("%s%03d", prefix, randomNumber);
      isUnique = userRepository.findByUsername(username).isEmpty();
    } while (!isUnique);

    return username;
  }

  private String generateRandomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}
