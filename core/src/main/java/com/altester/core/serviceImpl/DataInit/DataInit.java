package com.altester.core.serviceImpl.DataInit;

import com.altester.core.config.SemesterConfig;
import com.altester.core.exception.ResourceNotFoundException;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

  private static final String ADMIN_USERNAME = "ADMIN";
  private static final String STUDENT_USERNAME = "STUDENT";
  private static final String TEACHER_USERNAME = "TEACHER";
  private static final String SURNAME_SUPER = "Super";
  private static final String GROUP_ACTIVE = "active";

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

  @Transactional
  public void createDefaultUsers() {
    log.info("Creating default admin, student, and teacher users");

    // Create admin
    if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
      String encodedAdminPassword = passwordEncoder.encode(adminPassword);
      baseAdmin =
          User.builder()
              .name("Admin")
              .surname(SURNAME_SUPER)
              .email("admin@vsb.cz")
              .username(ADMIN_USERNAME)
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
      baseAdmin = userRepository.findByUsername(ADMIN_USERNAME).get();
      log.info("Admin user already exists: {}", baseAdmin.getUsername());
    }

    // Create base student
    if (userRepository.findByUsername(STUDENT_USERNAME).isEmpty()) {
      String encodedStudentPassword = passwordEncoder.encode(testStudentPassword);
      baseStudent =
          User.builder()
              .name("Student")
              .surname(SURNAME_SUPER)
              .email("student@vsb.cz")
              .username(STUDENT_USERNAME)
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
      baseStudent = userRepository.findByUsername(STUDENT_USERNAME).get();
      log.info("Base student already exists: {}", baseStudent.getUsername());
    }

    // Create base teacher
    if (userRepository.findByUsername(TEACHER_USERNAME).isEmpty()) {
      String encodedTeacherPassword = passwordEncoder.encode(testTeacherPassword);
      baseTeacher =
          User.builder()
              .name("Teacher")
              .surname(SURNAME_SUPER)
              .email("teacher@vsb.cz")
              .username(TEACHER_USERNAME)
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
      baseTeacher = userRepository.findByUsername(TEACHER_USERNAME).get();
      log.info("Base teacher already exists: {}", baseTeacher.getUsername());
    }
  }

  // Create students for testing
  @Transactional
  public void createStudents(int amount) {
    if (amount > DataConstants.SURNAMES.size()) {
      amount = DataConstants.SURNAMES.size();
    }

    log.info("Creating {} students", amount);

    Pageable pageable = PageRequest.of(0, 100);
    var studentPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);
    int currentCount = (int) studentPage.getTotalElements();

    int remainingToCreate = Math.max(0, amount - currentCount + 2);

    if (remainingToCreate == 0) {
      log.info("Number of students already exists ({}), skipping student creation", currentCount);
      return;
    }

    log.info("Creating {} additional students", remainingToCreate);

    for (int i = 0; i < remainingToCreate; i++) {
      String firstname = DataConstants.FIRSTNAMES.get(i % DataConstants.FIRSTNAMES.size());
      String lastname = DataConstants.SURNAMES.get(i % DataConstants.SURNAMES.size());
      String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + ".st@vsb.cz";

      if (userRepository.findByEmail(email).isPresent()) {
        log.info("Skipping existing student with email: {}", email);
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
      log.info("Created student: {} {}", firstname, lastname);
    }
  }

  @Transactional
  public void createTeachers(int amount) {
    if (amount > 45) {
      amount = 45;
    }

    log.info("Creating {} teachers", amount);

    Pageable pageable = PageRequest.of(0, 100);
    var teacherPage = userRepository.findByRole(RolesEnum.TEACHER, pageable);
    int currentCount = (int) teacherPage.getTotalElements();

    int remainingToCreate = Math.max(0, amount - currentCount + 1);

    if (remainingToCreate == 0) {
      log.info(
          "Adequate number of teachers already exists ({}), skipping teacher creation",
          currentCount);
      return;
    }

    log.info("Creating {} additional teachers", remainingToCreate);

    for (int i = 0; i < remainingToCreate; i++) {
      String firstname = DataConstants.FIRSTNAMES.get((i + 30) % DataConstants.FIRSTNAMES.size());
      String lastname = DataConstants.SURNAMES.get((i + 30) % DataConstants.SURNAMES.size());
      String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@vsb.cz";

      if (userRepository.findByEmail(email).isPresent()) {
        log.info("Skipping existing teacher with email: {}", email);
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
      log.info("Created teacher: {} {}", firstname, lastname);
    }
  }

  public String generateUsername(String surname) {
    String prefix = surname.substring(0, Math.min(3, surname.length())).toUpperCase();

    String username;
    boolean isUnique;

    do {
      int randomNumber = 100 + random.nextInt(900);
      username = String.format("%sR%03d", prefix, randomNumber);
      isUnique = userRepository.findByUsername(username).isEmpty();
    } while (!isUnique);

    return username;
  }

  @Transactional
  public void createSubject(int amount) {
    if (amount > DataConstants.SUBJECTS.size()) {
      amount = DataConstants.SUBJECTS.size();
    }

    log.info("Creating {} subjects", amount);

    for (int i = 0; i < amount; i++) {
      String subjectName = DataConstants.SUBJECTS.get(i);
      String shortName = DataConstants.SHORT_NAMES.get(i);
      String description =
          "This course covers "
              + subjectName
              + " concepts and practical applications. Students will learn fundamental principles "
              + "and develop skills through hands-on exercises and projects.";

      if (subjectRepository.findByShortName(shortName).isPresent()) {
        log.info("Skipping existing subject: {}", shortName);
        continue;
      }

      Subject subject =
          Subject.builder()
              .name(subjectName)
              .shortName(shortName)
              .description(description)
              .modified(LocalDateTime.now())
              .groups(new HashSet<>())
              .build();

      subjectRepository.save(subject);
      log.info("Created subject: {} {}", subjectName, shortName);
    }
  }

  @Transactional
  public void createStudentGroups() {
    Pageable pageable = PageRequest.of(0, 500);
    var studentPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);
    var teacherPage = userRepository.findByRole(RolesEnum.TEACHER, pageable);

    List<User> students = new ArrayList<>(studentPage.getContent());
    List<User> teachers = new ArrayList<>(teacherPage.getContent());

    // Get all subjects
    List<Subject> allSubjects = subjectRepository.findAll();
    List<Subject> subjects = new ArrayList<>(allSubjects);

    if (students.isEmpty()) {
      log.warn("No students available to create groups.");
      return;
    }

    if (teachers.isEmpty()) {
      log.warn("No teachers available to create groups.");
      return;
    }

    if (subjects.isEmpty()) {
      log.warn("No subjects available to create groups.");
      return;
    }

    Semester currentSemester = semesterConfig.getCurrentSemester();
    int currentYear = semesterConfig.getCurrentAcademicYear();

    log.info("Creating groups for current semester {} and year {}", currentSemester, currentYear);

    if (baseStudent != null && !students.contains(baseStudent)) {
      students.add(baseStudent);
    }

    if (baseTeacher != null && !teachers.contains(baseTeacher)) {
      teachers.add(baseTeacher);
    }

    // Track groups per subject to limit to 6 per subject in subsequent initializations
    Map<Long, Integer> subjectGroupCount = new HashMap<>();

    // Track base teacher's groups
    List<Group> baseTeacherGroups = new ArrayList<>();

    // Track which groups the base student is in
    List<Group> baseStudentGroups = new ArrayList<>();

    // Track student assignment to prevent duplicates in same subject/semester/year
    Map<String, Set<Long>> studentAssignments =
        new HashMap<>(); // Key: "studentId-subjectId-semester-year"

    // Process subjects in chunks to avoid memory issues
    int chunkSize = 5;
    for (int i = 0; i < subjects.size(); i += chunkSize) {
      int end = Math.min(i + chunkSize, subjects.size());
      List<Subject> subjectChunk = subjects.subList(i, end);

      // Create current semester groups (13 per subject)
      for (Subject subject : subjectChunk) {
        int maxGroupsForThisSubject =
            Math.min(13, 6 - subjectGroupCount.getOrDefault(subject.getId(), 0));
        createGroupsForSemester(
            subject,
            currentSemester,
            currentYear,
            maxGroupsForThisSubject,
            students,
            teachers,
            baseTeacherGroups,
            baseStudentGroups,
            studentAssignments,
            true);

        subjectGroupCount.put(
            subject.getId(),
            subjectGroupCount.getOrDefault(subject.getId(), 0) + maxGroupsForThisSubject);
      }
    }

    // Create previous semester groups (1 per subject)
    Semester prevSemester =
        (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
    int prevYear = (prevSemester == Semester.SUMMER) ? currentYear - 1 : currentYear;

    for (int i = 0; i < subjects.size(); i += chunkSize) {
      int end = Math.min(i + chunkSize, subjects.size());
      List<Subject> subjectChunk = subjects.subList(i, end);

      for (Subject subject : subjectChunk) {
        if (subjectGroupCount.getOrDefault(subject.getId(), 0) < 6) {
          createGroupsForSemester(
              subject,
              prevSemester,
              prevYear,
              1,
              students,
              teachers,
              baseTeacherGroups,
              baseStudentGroups,
              studentAssignments,
              false);

          subjectGroupCount.put(
              subject.getId(), subjectGroupCount.getOrDefault(subject.getId(), 0) + 1);
        }
      }
    }

    // Create current semester but previous year groups (1 per subject)
    int prevYearSameS = currentYear - 1;

    for (int i = 0; i < subjects.size(); i += chunkSize) {
      int end = Math.min(i + chunkSize, subjects.size());
      List<Subject> subjectChunk = subjects.subList(i, end);

      for (Subject subject : subjectChunk) {
        if (subjectGroupCount.getOrDefault(subject.getId(), 0) < 6) {
          createGroupsForSemester(
              subject,
              currentSemester,
              prevYearSameS,
              1,
              students,
              teachers,
              baseTeacherGroups,
              baseStudentGroups,
              studentAssignments,
              false);

          subjectGroupCount.put(
              subject.getId(), subjectGroupCount.getOrDefault(subject.getId(), 0) + 1);
        }
      }
    }

    // Create next semester groups (1 per subject)
    Semester nextSemester =
        (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
    int nextYear = (nextSemester == Semester.WINTER) ? currentYear + 1 : currentYear;

    for (int i = 0; i < subjects.size(); i += chunkSize) {
      int end = Math.min(i + chunkSize, subjects.size());
      List<Subject> subjectChunk = subjects.subList(i, end);

      for (Subject subject : subjectChunk) {
        if (subjectGroupCount.getOrDefault(subject.getId(), 0) < 6) {
          createGroupsForSemester(
              subject,
              nextSemester,
              nextYear,
              1,
              students,
              teachers,
              baseTeacherGroups,
              baseStudentGroups,
              studentAssignments,
              false);

          subjectGroupCount.put(
              subject.getId(), subjectGroupCount.getOrDefault(subject.getId(), 0) + 1);
        }
      }
    }

    // Ensure base teacher has appropriate number of groups
    ensureBaseTeacherGroups(baseTeacherGroups, currentSemester, currentYear, subjects);

    // Ensure base student is in 5-6 different groups
    ensureBaseStudentGroups(
        baseStudentGroups,
        students,
        teachers,
        subjects,
        currentSemester,
        currentYear,
        studentAssignments);

    log.info("Group creation complete. Created groups for {} subjects", subjects.size());
    log.info("Base teacher assigned to {} groups", baseTeacherGroups.size());
    log.info("Base student assigned to {} groups", baseStudentGroups.size());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createGroupsForSemester(
      Subject subject,
      Semester semester,
      int academicYear,
      int groupCount,
      List<User> students,
      List<User> teachers,
      List<Group> baseTeacherGroups,
      List<Group> baseStudentGroups,
      Map<String, Set<Long>> studentAssignments,
      boolean prioritizeBaseUsers) {

    log.info(
        "Creating {} groups for subject {} in semester {} year {}",
        groupCount,
        subject.getShortName(),
        semester,
        academicYear);

    for (int i = 0; i < groupCount; i++) {
      String groupName = generateGroupName(i, semester, academicYear, i, subject.getShortName());

      if (groupRepository.findByName(groupName).isPresent()) {
        log.info("Skipping existing group: {}", groupName);
        continue;
      }

      User teacher;
      boolean isBaseTeacherGroup = false;

      if (prioritizeBaseUsers && baseTeacher != null && i == 0 && baseTeacherGroups.size() < 4) {
        teacher = baseTeacher;
        isBaseTeacherGroup = true;
      } else if (!prioritizeBaseUsers
          && baseTeacher != null
          && baseTeacherGroups.size() < 8
          && random.nextDouble() < 0.3) {
        teacher = baseTeacher;
        isBaseTeacherGroup = true;
      } else {
        teacher = teachers.get(random.nextInt(teachers.size()));
      }

      // Determine if group is active based on semester and year
      boolean groupIsActive =
          !groupActivityService.isGroupInFuture(
              Group.builder().semester(semester).academicYear(academicYear).build());

      // Create and save the group
      Group group =
          Group.builder()
              .name(groupName)
              .teacher(teacher)
              .students(new HashSet<>()) // Empty students initially
              .semester(semester)
              .academicYear(academicYear)
              .active(groupIsActive)
              .tests(new HashSet<>()) // Empty tests initially
              .build();

      Group savedGroup = groupRepository.save(group);
      log.info(
          "Created group: {} for {}/{} - teacher: {} - {}",
          groupName,
          semester,
          academicYear,
          teacher.getUsername(),
          groupIsActive ? GROUP_ACTIVE : "inactive");

      // Add students to group
      int studentCount = 10 + random.nextInt(11); // 10-20 students
      populateGroupWithStudents(
          savedGroup,
          subject,
          semester,
          academicYear,
          students,
          studentCount,
          studentAssignments,
          baseStudentGroups);

      if (isBaseTeacherGroup) {
        baseTeacherGroups.add(savedGroup);
      }

      entityManager.flush();
      entityManager.clear();

      // Re-fetch the group from database
      Group managedGroup = groupRepository.findById(savedGroup.getId()).orElse(savedGroup);

      // Associate the group with its subject
      Subject managedSubject = subjectRepository.findById(subject.getId()).orElse(subject);

      Set<Group> subjectGroups = new HashSet<>();
      if (managedSubject.getGroups() != null) {
        subjectGroups.addAll(managedSubject.getGroups());
      }
      subjectGroups.add(managedGroup);
      managedSubject.setGroups(subjectGroups);

      subjectRepository.save(managedSubject);

      createTestsForGroup(managedGroup, managedSubject);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void populateGroupWithStudents(
      Group group,
      Subject subject,
      Semester semester,
      int academicYear,
      List<User> students,
      int targetStudentCount,
      Map<String, Set<Long>> studentAssignments,
      List<Group> baseStudentGroups) {

    Set<User> groupStudents = new HashSet<>();
    boolean includeBaseStudent =
        baseStudent != null
            && (baseStudentGroups.size() < 5
                || (baseStudentGroups.size() < 6 && random.nextDouble() < 0.7));

    if (includeBaseStudent) {
      String key =
          baseStudent.getId() + "-" + subject.getId() + "-" + semester + "-" + academicYear;

      if (!studentAssignments.containsKey(key)
          || !studentAssignments.get(key).contains(subject.getId())) {
        groupStudents.add(baseStudent);

        studentAssignments.computeIfAbsent(key, k -> new HashSet<>()).add(subject.getId());

        baseStudentGroups.add(group);
      }
    }

    int attempts = 0;
    int maxAttempts = students.size() * 2;

    while (groupStudents.size() < targetStudentCount && attempts < maxAttempts) {
      User candidate = students.get(random.nextInt(students.size()));
      String key = candidate.getId() + "-" + subject.getId() + "-" + semester + "-" + academicYear;

      // Check if student is already in a group for this subject/semester/year
      if (!studentAssignments.containsKey(key)
          || !studentAssignments.get(key).contains(subject.getId())) {
        groupStudents.add(candidate);

        // Track assignment
        studentAssignments.computeIfAbsent(key, k -> new HashSet<>()).add(subject.getId());
      }

      attempts++;
    }

    Group managedGroup = groupRepository.findById(group.getId()).orElse(group);
    managedGroup.setStudents(groupStudents);
    groupRepository.save(managedGroup);

    log.info("Added {} students to group {}", groupStudents.size(), group.getName());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void ensureBaseTeacherGroups(
      List<Group> baseTeacherGroups,
      Semester currentSemester,
      int currentYear,
      List<Subject> subjects) {

    if (baseTeacher == null) {
      log.warn("Base teacher not found, skipping ensuring base teacher groups");
      return;
    }

    int currentGroups = 0;
    int pastGroups = 0;
    int futureGroups = 0;

    for (Group group : baseTeacherGroups) {
      if (group.getSemester() == currentSemester && group.getAcademicYear() == currentYear) {
        currentGroups++;
      } else if (groupActivityService.isGroupInFuture(group)) {
        futureGroups++;
      } else {
        pastGroups++;
      }
    }

    log.info(
        "Base teacher has {} current groups, {} past groups, {} future groups",
        currentGroups,
        pastGroups,
        futureGroups);

    if (currentGroups < 4 && !subjects.isEmpty()) {
      int needed = 4 - currentGroups;
      for (int i = 0; i < needed && i < subjects.size(); i++) {
        Subject subject = subjects.get(i);
        createBaseTeacherGroup(subject, currentSemester, currentYear, false);
      }
    }

    if (pastGroups < 2 && !subjects.isEmpty()) {
      int needed = 2 - pastGroups;
      Semester prevSemester =
          (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
      int prevYear = (prevSemester == Semester.SUMMER) ? currentYear - 1 : currentYear;

      for (int i = 0; i < needed && i < subjects.size(); i++) {
        Subject subject = subjects.get(subjects.size() - i - 1); // Use different subjects
        createBaseTeacherGroup(subject, prevSemester, prevYear, true);
      }
    }

    if (futureGroups < 2 && subjects.size() > 5) {
      int needed = 2 - futureGroups;
      Semester nextSemester =
          (currentSemester == Semester.WINTER) ? Semester.SUMMER : Semester.WINTER;
      int nextYear = (nextSemester == Semester.WINTER) ? currentYear + 1 : currentYear;

      for (int i = 0; i < needed && i < subjects.size() - 5; i++) {
        Subject subject = subjects.get(i + 5); // Use different subjects
        createBaseTeacherGroup(subject, nextSemester, nextYear, false);
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createBaseTeacherGroup(
      Subject subject, Semester semester, int academicYear, boolean isPast) {

    String groupName = generateGroupName(999, semester, academicYear, 0, subject.getShortName());
    groupName = "BT-" + groupName;

    if (groupRepository.findByName(groupName).isPresent()) {
      log.info("Skipping existing base teacher group: {}", groupName);
      return;
    }

    boolean isActive = true;
    if (groupActivityService.isGroupInFuture(
        Group.builder().semester(semester).academicYear(academicYear).build())) {
      isActive = false;
    } else if (isPast) {
      isActive = false;
    }

    Group group =
        Group.builder()
            .name(groupName)
            .teacher(baseTeacher)
            .students(new HashSet<>())
            .semester(semester)
            .academicYear(academicYear)
            .active(isActive)
            .tests(new HashSet<>())
            .build();

    Group savedGroup = groupRepository.save(group);

    // Add random students
    Pageable pageable = PageRequest.of(0, 100);
    var studentPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);

    List<User> students = new ArrayList<>(studentPage.getContent());
    int studentCount = 10 + random.nextInt(11);
    Set<User> groupStudents = new HashSet<>();

    for (int i = 0; i < studentCount && i < students.size(); i++) {
      groupStudents.add(students.get(random.nextInt(students.size())));
    }

    // Add base student too
    if (baseStudent != null) {
      groupStudents.add(baseStudent);
    }

    Group managedGroup = groupRepository.findById(savedGroup.getId()).orElse(savedGroup);
    managedGroup.setStudents(groupStudents);
    groupRepository.save(managedGroup);

    Subject managedSubject = subjectRepository.findById(subject.getId()).orElse(subject);
    Set<Group> subjectGroups = new HashSet<>();
    if (managedSubject.getGroups() != null) {
      subjectGroups.addAll(managedSubject.getGroups());
    }
    subjectGroups.add(managedGroup);
    managedSubject.setGroups(subjectGroups);
    subjectRepository.save(managedSubject);

    entityManager.flush();
    entityManager.clear();

    managedGroup = groupRepository.findById(managedGroup.getId()).orElse(managedGroup);
    managedSubject = subjectRepository.findById(managedSubject.getId()).orElse(managedSubject);

    // Create tests
    createTestsForGroup(managedGroup, managedSubject);

    log.info(
        "Created special base teacher group: {} for {}/{} with {} students - {}",
        groupName,
        semester,
        academicYear,
        groupStudents.size(),
        isActive ? GROUP_ACTIVE : "inactive");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void ensureBaseStudentGroups(
      List<Group> baseStudentGroups,
      List<User> students,
      List<User> teachers,
      List<Subject> subjects,
      Semester currentSemester,
      int currentYear,
      Map<String, Set<Long>> studentAssignments) {

    if (baseStudent == null) {
      log.warn("Base student not found, skipping ensuring base student groups");
      return;
    }

    // Ensure base student is in 5-6 groups
    if (baseStudentGroups.size() < 5 && subjects.size() > 10) {
      int needed = 5 - baseStudentGroups.size();
      log.info("Adding base student to {} more groups to reach minimum of 5", needed);

      for (int i = 0; i < needed && i < subjects.size() - 10; i++) {
        Subject subject = subjects.get(i + 10);

        Subject managedSubject = subjectRepository.findById(subject.getId()).orElse(subject);
        Set<Group> existingGroups = managedSubject.getGroups();

        List<Group> subjectGroups =
            existingGroups != null ? new ArrayList<>(existingGroups) : new ArrayList<>();

        Group targetGroup = null;

        if (!subjectGroups.isEmpty()) {
          for (Group group : subjectGroups) {
            Set<User> groupStudents = group.getStudents();
            boolean alreadyInGroup = groupStudents != null && groupStudents.contains(baseStudent);
            boolean correctSemesterAndYear =
                group.getSemester() == currentSemester && group.getAcademicYear() == currentYear;

            if (!alreadyInGroup && correctSemesterAndYear) {
              targetGroup = group;
              break;
            }
          }
        }

        if (targetGroup != null) {
          Group managedGroup = groupRepository.findById(targetGroup.getId()).orElse(targetGroup);

          Set<User> groupStudents =
              managedGroup.getStudents() != null
                  ? new HashSet<>(managedGroup.getStudents())
                  : new HashSet<>();

          groupStudents.add(baseStudent);
          managedGroup.setStudents(groupStudents);
          groupRepository.save(managedGroup);
          baseStudentGroups.add(targetGroup);

          log.info("Added base student to existing group: {}", targetGroup.getName());
          continue;
        }

        String groupName =
            "BS-" + generateGroupName(888, currentSemester, currentYear, i, subject.getShortName());

        if (groupRepository.findByName(groupName).isPresent()) {
          log.info("Skipping existing base student group: {}", groupName);
        } else {
          User teacher = teachers.get(random.nextInt(teachers.size()));

          Group group =
              Group.builder()
                  .name(groupName)
                  .teacher(teacher)
                  .students(new HashSet<>())
                  .semester(currentSemester)
                  .academicYear(currentYear)
                  .active(true)
                  .tests(new HashSet<>())
                  .build();

          Set<User> groupStudents = new HashSet<>();
          groupStudents.add(baseStudent);

          for (int j = 0; j < 15 && j < students.size(); j++) {
            User student = students.get(random.nextInt(students.size()));
            if (!student.equals(baseStudent)) {
              groupStudents.add(student);
            }
          }

          group.setStudents(groupStudents);
          Group savedGroup = groupRepository.save(group);
          baseStudentGroups.add(savedGroup);

          // Associate with subject
          managedSubject = subjectRepository.findById(subject.getId()).orElse(subject);
          Set<Group> subjectGroupSet =
              managedSubject.getGroups() != null
                  ? new HashSet<>(managedSubject.getGroups())
                  : new HashSet<>();
          subjectGroupSet.add(savedGroup);
          managedSubject.setGroups(subjectGroupSet);
          subjectRepository.save(managedSubject);

          // Clear persistence context
          entityManager.flush();
          entityManager.clear();

          // Re-fetch for test creation
          savedGroup = groupRepository.findById(savedGroup.getId()).orElse(savedGroup);
          managedSubject =
              subjectRepository.findById(managedSubject.getId()).orElse(managedSubject);

          // Create tests
          createTestsForGroup(savedGroup, managedSubject);

          log.info(
              "Created special base student group: {} with {} students",
              groupName,
              groupStudents.size());
        }
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createTestsForGroup(Group group, Subject subject) {
    int baseTestCount = group.isActive() ? 3 : 2;
    int variance = group.isActive() ? 3 : 1;
    int testCount = baseTestCount + random.nextInt(variance);

    for (int i = 0; i < testCount; i++) {
      String testTitle = getTestTitleForSubject(subject, i);
      String description =
          DataConstants.TEST_DESCRIPTIONS.get(i % DataConstants.TEST_DESCRIPTIONS.size());

      boolean createdByAdmin = i % 3 == 0;
      boolean allowTeacherEdit = i % 3 == 1 || !createdByAdmin;

      LocalDateTime startTime;
      LocalDateTime endTime;

      if (!group.isActive() && !groupActivityService.isGroupInFuture(group)) {
        startTime = LocalDateTime.now().minusMonths(3).plusDays(i * 3L);
        endTime = startTime.plusDays(14);
      } else if (groupActivityService.isGroupInFuture(group)) {
        startTime = LocalDateTime.now().plusMonths(1).plusDays(i * 3L);
        endTime = startTime.plusDays(14);
      } else {
        if (i % 3 == 0) {
          startTime = LocalDateTime.now().minusDays(30L + random.nextInt(30));
          endTime = startTime.plusDays(7);
        } else if (i % 3 == 1) {
          startTime = LocalDateTime.now().minusDays(3);
          endTime = LocalDateTime.now().plusDays(4);
        } else {
          startTime = LocalDateTime.now().plusDays(7L + random.nextInt(21));
          endTime = startTime.plusDays(7);
        }
      }

      // Randomize number of questions between 1-5 for each difficulty
      int easyCount = 1 + random.nextInt(5);
      int mediumCount = 1 + random.nextInt(5);
      int hardCount = 1 + random.nextInt(5);

      if (i % 3 == 2) {
        easyCount += 1;
        mediumCount += 2;
        hardCount += 1;
      }

      boolean isOpenTest = group.isActive() && i % 2 == 0;

      Test test =
          Test.builder()
              .title(testTitle)
              .description(description)
              .duration(30 + (i * 15))
              .isOpen(isOpenTest)
              .maxAttempts(1 + i % 3)
              .easyQuestionsCount(easyCount)
              .mediumQuestionsCount(mediumCount)
              .hardQuestionsCount(hardCount)
              .easyQuestionScore(2 + random.nextInt(3))
              .mediumQuestionScore(4 + random.nextInt(3))
              .hardQuestionScore(7 + random.nextInt(4))
              .startTime(startTime)
              .endTime(endTime)
              .isCreatedByAdmin(createdByAdmin)
              .allowTeacherEdit(allowTeacherEdit)
              .build();

      Test savedTest = testRepository.save(test);

      Group managedGroup = groupRepository.findById(group.getId()).orElse(group);
      Set<Test> groupTests = new HashSet<>();
      if (managedGroup.getTests() != null) {
        groupTests.addAll(managedGroup.getTests());
      }
      groupTests.add(savedTest);
      managedGroup.setTests(groupTests);
      groupRepository.save(managedGroup);

      entityManager.flush();
      entityManager.clear();

      savedTest = testRepository.findById(savedTest.getId()).orElse(savedTest);

      int questionMultiplier = group.isActive() ? 1 : 0;
      createQuestionsForTest(savedTest, subject, questionMultiplier);

      // Create test attempts if group is active
      if (group.isActive() && savedTest.isOpen()) {
        createTestAttemptsForGroup(savedTest, group);
      }

      if (group.isActive() && i % 3 == 0 && group.getTeacher().equals(baseTeacher)) {
        assignApiKeyAndPromptToTest(savedTest, group);
      }

      String groupStatus;
      if (group.isActive()) {
        groupStatus = GROUP_ACTIVE;
      } else if (groupActivityService.isGroupInFuture(group)) {
        groupStatus = "future";
      } else {
        groupStatus = "past";
      }

      log.info(
          "Created test '{}' for group {} - {} ({})",
          testTitle,
          managedGroup.getName(),
          createdByAdmin ? "admin created" : "teacher created",
          groupStatus);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void assignApiKeyAndPromptToTest(Test test, Group group) {
    try {
      ApiKey apiKey = getOrCreateApiKey();

      Prompt prompt = getOrCreatePrompt();

      if (apiKey != null && prompt != null) {
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
        log.info(
            "Assigned API key and prompt to test '{}' for group '{}'",
            test.getTitle(),
            group.getName());
      }
    } catch (Exception e) {
      log.error("Error assigning API key and prompt to test: {}", e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public ApiKey getOrCreateApiKey() {
    List<ApiKey> existingKeys = apiKeyRepository.findAll();

    if (!existingKeys.isEmpty()) {
      return existingKeys.get(random.nextInt(existingKeys.size()));
    }

    List<AiServiceName> services = Arrays.asList(AiServiceName.values());

    for (int i = 0; i < 5; i++) {
      AiServiceName service = services.get(i % services.size());
      String keyPrefix = generateRandomString(10);
      String keySuffix = generateRandomString(10);

      String model;
      switch (service) {
        case AiServiceName.OPENAI -> model = "gpt-4";
        case AiServiceName.ANTHROPIC_CLAUDE -> model = "claude-3-opus-20240229";
        case AiServiceName.GEMINI -> model = "gemini-pro";
        default -> model = null;
      }

      User owner = (i % 2 == 0) ? baseAdmin : baseTeacher;

      ApiKey apiKey =
          ApiKey.builder()
              .name("API Key " + (i + 1))
              .encryptedKey(passwordEncoder.encode(keyPrefix + "..." + keySuffix))
              .keyPrefix(keyPrefix)
              .keySuffix(keySuffix)
              .aiServiceName(service)
              .model(model)
              .isGlobal(i == 0)
              .owner(owner)
              .createdAt(LocalDateTime.now())
              .description("Test API key for " + service)
              .isActive(true)
              .build();

      apiKeyRepository.save(apiKey);
      log.info("Created API key for service: {}", service);
    }

    existingKeys = apiKeyRepository.findAll();
    return existingKeys.isEmpty() ? null : existingKeys.getFirst();
  }

  private String generateRandomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(chars.length());
      sb.append(chars.charAt(index));
    }
    return sb.toString();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Prompt getOrCreatePrompt() {
    Prompt defaultPrompt = promptRepository.findById(1L).orElse(null);

    // Check if we have other prompts
    List<Prompt> existingPrompts = promptRepository.findAll();
    if (!existingPrompts.isEmpty() && !existingPrompts.contains(defaultPrompt)) {
      return existingPrompts.get(random.nextInt(existingPrompts.size()));
    }

    if (defaultPrompt == null) {
      createDefaultPrompt();
      promptRepository.findById(1L);
    }

    // Create additional prompts
    createAdditionalPrompts();

    // Return one of the prompts
    existingPrompts = promptRepository.findAll();
    return existingPrompts.isEmpty()
        ? null
        : existingPrompts.get(random.nextInt(existingPrompts.size()));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createTestAttemptsForGroup(Test test, Group group) {
    try {
      Test managedTest = testRepository.findById(test.getId()).orElse(test);
      Group managedGroup = groupRepository.findById(group.getId()).orElse(group);

      Set<User> students = new HashSet<>();
      if (managedGroup.getStudents() != null) {
        students.addAll(managedGroup.getStudents());
      }

      for (User student : students) {
        if (random.nextDouble() > 0.5 && !student.equals(baseStudent)) {
          continue;
        }

        boolean isAiGraded = false;

        for (TestGroupAssignment tga : managedTest.getTestGroupAssignments()) {
          if (tga.getGroup().equals(managedGroup) && tga.isAiEvaluation()) {
            isAiGraded = true;
            break;
          }
        }

        int maxAttempts = managedTest.getMaxAttempts() != null ? managedTest.getMaxAttempts() : 1;
        int attemptsToCreate = 1 + random.nextInt(maxAttempts);

        for (int i = 0; i < attemptsToCreate; i++) {
          LocalDateTime startTime = managedTest.getStartTime().plusHours(random.nextInt(24));
          LocalDateTime endTime = null;

          AttemptStatus status;
          if (i == attemptsToCreate - 1 && random.nextDouble() < 0.2) {
            status = AttemptStatus.IN_PROGRESS;
          } else {
            endTime = startTime.plusMinutes(random.nextInt(managedTest.getDuration()));

            if (isAiGraded && random.nextDouble() < 0.7) {
              status = AttemptStatus.AI_REVIEWED;
            } else {
              status = AttemptStatus.REVIEWED;
            }
          }

          Attempt attempt =
              Attempt.builder()
                  .attemptNumber(i + 1)
                  .startTime(startTime)
                  .endTime(endTime)
                  .status(status)
                  .test(managedTest)
                  .student(student)
                  .aiGradingSentAt(
                      status == AttemptStatus.AI_REVIEWED ? endTime.plusMinutes(5) : null)
                  .build();

          Attempt savedAttempt = attemptRepository.save(attempt);

          if (status != AttemptStatus.IN_PROGRESS) {
            createSubmissionsForAttempt(savedAttempt, managedTest, isAiGraded);
          }

          log.debug(
              "Created attempt {} for student {} on test '{}'",
              i + 1,
              student.getUsername(),
              managedTest.getTitle());
        }
      }
    } catch (Exception e) {
      log.error("Error creating test attempts: {}", e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createSubmissionsForAttempt(Attempt attempt, Test test, boolean isAiGraded) {
    try {
      Attempt managedAttempt = attemptRepository.findById(attempt.getId()).orElse(attempt);
      Test managedTest = testRepository.findById(test.getId()).orElse(test);

      List<Question> questions = new ArrayList<>();
      if (managedTest.getQuestions() != null) {
        questions.addAll(managedTest.getQuestions());
      }

      int totalScore = 0;
      int aiTotalScore = 0;

      for (int i = 0; i < questions.size(); i++) {
        Question question = questions.get(i);

        String answerText = null;
        List<Option> selectedOptions = new ArrayList<>();

        // Generate student answer based on question type
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
            || question.getQuestionType() == QuestionType.IMAGE_WITH_MULTIPLE_CHOICE) {

          // Get a modifiable list of options
          List<Option> options = new ArrayList<>();
          if (question.getOptions() != null) {
            options.addAll(question.getOptions());
          }

          // Randomly select correct or incorrect options
          boolean selectsCorrect = random.nextDouble() < 0.7; // 70% chance of correct

          for (Option option : options) {
            if (option.isCorrect() && selectsCorrect) {
              selectedOptions.add(option);
            } else if (!option.isCorrect()
                && random.nextDouble() < 0.2) { // sometimes pick wrong ones
              selectedOptions.add(option);
            }
          }

          if (selectedOptions.isEmpty() && !options.isEmpty()) {
            selectedOptions.add(options.get(random.nextInt(options.size())));
          }

        } else {
          answerText = generateStudentAnswer(question, random.nextDouble() < 0.8); // 80% correct
        }

        int questionMaxScore = question.getScore();
        int questionScore;
        int aiQuestionScore;

        if (answerText != null) {
          double qualityFactor = random.nextDouble();
          questionScore = (int) (questionMaxScore * (0.5 + qualityFactor * 0.5));
          aiQuestionScore = (int) (questionMaxScore * (0.4 + qualityFactor * 0.6));
        } else {
          boolean allCorrect = true;
          for (Option option : question.getOptions()) {
            boolean isCorrect = option.isCorrect();
            boolean isSelected = selectedOptions.contains(option);

            if ((isCorrect && !isSelected) || (!isCorrect && isSelected)) {
              allCorrect = false;
              break;
            }
          }

          if (allCorrect) {
            questionScore = questionMaxScore;
            aiQuestionScore = questionMaxScore;
          } else {
            questionScore = (int) (questionMaxScore * (0.2 + random.nextInt() * 0.5));
            aiQuestionScore = (int) (questionMaxScore * (0.1 + random.nextInt() * 0.6));
          }
        }

        totalScore += questionScore;
        aiTotalScore += aiQuestionScore;

        Submission submission =
            Submission.builder()
                .answerText(answerText)
                .score(questionScore)
                .aiScore(aiQuestionScore)
                .attempt(managedAttempt)
                .question(question)
                .orderIndex(i)
                .build();

        if (managedAttempt.getStatus() == AttemptStatus.REVIEWED
            || managedAttempt.getStatus() == AttemptStatus.AI_REVIEWED) {
          submission.setAiFeedback(generateAIFeedback(questionScore, questionMaxScore));
          submission.setAiGraded(true);

          if (managedAttempt.getStatus() == AttemptStatus.REVIEWED) {
            submission.setTeacherFeedback(generateTeacherFeedback(questionScore, questionMaxScore));
          }
        }

        Submission savedSubmission = submissionRepository.save(submission);

        if (!selectedOptions.isEmpty()) {
          Submission managedSubmission =
              submissionRepository.findById(savedSubmission.getId()).orElse(savedSubmission);
          managedSubmission.setSelectedOptions(selectedOptions);
          submissionRepository.save(managedSubmission);
        }
      }

      // Update attempt with total scores
      managedAttempt.setScore(totalScore);
      managedAttempt.setAiScore(aiTotalScore);
      attemptRepository.save(managedAttempt);
    } catch (Exception e) {
      log.error("Error creating submissions: {}", e.getMessage());
    }
  }

  private String generateStudentAnswer(Question question, boolean isCorrect) {
    if (isCorrect) {
      return "This is a correct student answer that addresses the question about "
          + question.getQuestionText()
          + ". The answer includes all key points and demonstrates good understanding of the concept.";
    } else {
      return "This is a partially correct answer to the question. Some key points are missing and there are minor misconceptions about "
          + question
              .getQuestionText()
              .substring(0, Math.min(50, question.getQuestionText().length()))
          + ".";
    }
  }

  private String generateAIFeedback(int score, int maxScore) {
    double percentage = (double) score / maxScore;

    if (percentage > 0.9) {
      return "Excellent work! Your answer demonstrates a thorough understanding of the concept. All key points are addressed accurately.";
    } else if (percentage > 0.7) {
      return "Good job! Your answer shows solid understanding, but could be more comprehensive. Consider elaborating on [specific aspect].";
    } else if (percentage > 0.5) {
      return "Satisfactory answer with some good points. However, there are areas that need improvement, particularly regarding [concept].";
    } else {
      return "This answer needs significant improvement. Key concepts are missing or misunderstood. Please review [specific topic] in the course materials.";
    }
  }

  private String generateTeacherFeedback(int score, int maxScore) {
    double percentage = (double) score / maxScore;

    if (percentage > 0.8) {
      return "I'm impressed with your understanding! You've clearly put effort into mastering this material.";
    } else if (percentage > 0.6) {
      return "Good effort, but I'd like to see more depth in your explanation. Let's discuss this further in class.";
    } else {
      return "I encourage you to revisit this topic and come to office hours if you're having trouble. We can work through this together.";
    }
  }

  private String getTestTitleForSubject(Subject subject, int index) {
    String shortName = subject.getShortName();
    List<String> testTitles;

    switch (shortName) {
      case "PF", "DSA", "OOP":
        testTitles = DataConstants.TEST_TITLES_PROGRAMMING;
        break;
      case "DBS":
        testTitles = DataConstants.TEST_TITLES_DATABASES;
        break;
      case "CN", "NSEC":
        testTitles = DataConstants.TEST_TITLES_NETWORKS;
        break;
      case "OS":
        testTitles = DataConstants.TEST_TITLES_OS;
        break;
      case "AI", "ML", "CV", "NLP":
        testTitles = DataConstants.TEST_TITLES_AI;
        break;
      case "SE", "WD":
        testTitles = DataConstants.TEST_TITLES_SE;
        break;
      case "IS", "CYB":
        testTitles = DataConstants.TEST_TITLES_SECURITY;
        break;
      default:
        return subject.getShortName() + " - Test " + (index + 1);
    }

    return subject.getShortName() + ": " + testTitles.get(index % testTitles.size());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createQuestionsForTest(Test test, Subject subject, int questionMultiplier) {
    try {
      Test managedTest = testRepository.findById(test.getId()).orElse(test);

      int easyQuestionsToCreate =
          Math.max(
              managedTest.getEasyQuestionsCount() != null
                  ? managedTest.getEasyQuestionsCount() + 1
                  : 3,
              3);
      int mediumQuestionsToCreate =
          Math.max(
              managedTest.getMediumQuestionsCount() != null
                  ? managedTest.getMediumQuestionsCount() + 2
                  : 4,
              4);
      int hardQuestionsToCreate =
          Math.max(
              managedTest.getHardQuestionsCount() != null
                  ? managedTest.getHardQuestionsCount() + 1
                  : 2,
              2);

      if (questionMultiplier > 0) {
        easyQuestionsToCreate += random.nextInt(3);
        mediumQuestionsToCreate += random.nextInt(4);
        hardQuestionsToCreate += random.nextInt(2);
      }

      int easyQuestionScore =
          managedTest.getEasyQuestionScore() != null
              ? managedTest.getEasyQuestionScore()
              : 2 + random.nextInt(2);
      int mediumQuestionScore =
          managedTest.getMediumQuestionScore() != null
              ? managedTest.getMediumQuestionScore()
              : 4 + random.nextInt(3);
      int hardQuestionScore =
          managedTest.getHardQuestionScore() != null
              ? managedTest.getHardQuestionScore()
              : 7 + random.nextInt(4);

      createQuestionsWithDifficulty(
          managedTest, subject, easyQuestionsToCreate, QuestionDifficulty.EASY, easyQuestionScore);

      entityManager.flush();
      entityManager.clear();

      managedTest = testRepository.findById(managedTest.getId()).orElse(managedTest);

      createQuestionsWithDifficulty(
          managedTest,
          subject,
          mediumQuestionsToCreate,
          QuestionDifficulty.MEDIUM,
          mediumQuestionScore);

      entityManager.flush();
      entityManager.clear();

      managedTest = testRepository.findById(managedTest.getId()).orElse(managedTest);

      createQuestionsWithDifficulty(
          managedTest, subject, hardQuestionsToCreate, QuestionDifficulty.HARD, hardQuestionScore);

      log.info(
          "Created {} questions for test '{}' ({} easy @ {} pts, {} medium @ {} pts, {} hard @ {} pts)",
          easyQuestionsToCreate + mediumQuestionsToCreate + hardQuestionsToCreate,
          managedTest.getTitle(),
          easyQuestionsToCreate,
          easyQuestionScore,
          mediumQuestionsToCreate,
          mediumQuestionScore,
          hardQuestionsToCreate,
          hardQuestionScore);
    } catch (Exception e) {
      log.error("Error creating questions for test: {}", e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createQuestionsWithDifficulty(
      Test test, Subject subject, int count, QuestionDifficulty difficulty, int score) {
    // Create x2 the number of questions of each type as requested

    // Create multiple choice questions
    for (int i = 0; i < count; i++) {
      QuestionType questionType = QuestionType.MULTIPLE_CHOICE;

      String questionText = getQuestionTextForDifficulty(subject, i, difficulty);
      String correctAnswer =
          "This is the model answer for the multiple choice question: " + questionText;

      Question question =
          Question.builder()
              .questionText(questionText)
              .imagePath(null)
              .score(score)
              .correctAnswer(correctAnswer)
              .questionType(questionType)
              .difficulty(difficulty)
              .test(test)
              .build();

      Question savedQuestion = questionRepository.save(question);
      createOptionsForQuestion(savedQuestion, subject, i);

      log.debug(
          "Created {} {} multiple choice question for test '{}'",
          difficulty,
          questionType,
          test.getTitle());
    }

    // Create text-only questions
    for (int i = 0; i < count; i++) {
      QuestionType questionType = QuestionType.TEXT_ONLY;

      String questionText = getQuestionTextForDifficulty(subject, i + 100, difficulty);
      String correctAnswer =
          "This is the model answer for the text-only question: "
              + questionText
              + " It covers all the key points and demonstrates comprehensive understanding of the topic.";

      Question question =
          Question.builder()
              .questionText(questionText)
              .imagePath(null)
              .score(score)
              .correctAnswer(correctAnswer)
              .questionType(questionType)
              .difficulty(difficulty)
              .test(test)
              .build();

      questionRepository.save(question);

      log.debug(
          "Created {} {} text-only question for test '{}'",
          difficulty,
          questionType,
          test.getTitle());
    }
  }

  private String getQuestionTextForDifficulty(
      Subject subject, int index, QuestionDifficulty difficulty) {
    String shortName = subject.getShortName();
    String difficultyPrefix =
        switch (difficulty) {
          case EASY -> "Define";
          case MEDIUM -> "Explain";
          case HARD -> "Analyze";
        };

    List<String> questions;

    switch (shortName) {
      case "PF", "DSA", "OOP" -> {
        questions = DataConstants.CODING_QUESTIONS;
        String baseQuestion = questions.get(index % questions.size());
        return switch (difficulty) {
          case QuestionDifficulty.EASY -> "Basic: " + baseQuestion;
          case QuestionDifficulty.MEDIUM -> "Intermediate: " + baseQuestion;
          default -> "Advanced: " + baseQuestion;
        };
      }
      case "DBS" -> {
        questions = DataConstants.DATABASE_QUESTIONS;
        String baseQuestion = questions.get(index % questions.size());
        return difficultyPrefix + " - " + baseQuestion;
      }
      case "CN", "NSEC" -> {
        questions = DataConstants.NETWORKING_QUESTIONS;
        String baseQuestion = questions.get(index % questions.size());
        return difficultyPrefix + " - " + baseQuestion;
      }
      case "AI", "ML", "CV", "NLP" -> {
        questions = DataConstants.AI_QUESTIONS;
        String baseQuestion = questions.get(index % questions.size());
        return difficultyPrefix + " - " + baseQuestion;
      }
      case "SE", "WD" -> {
        questions = DataConstants.SOFTWARE_ENGINEERING_QUESTIONS;
        String baseQuestion = questions.get(index % questions.size());
        return difficultyPrefix + " - " + baseQuestion;
      }
      default -> {
        return difficultyPrefix + " the concept of " + getConceptForSubject(subject, index);
      }
    }
  }

  private String getConceptForSubject(Subject subject, int index) {
    String shortName = subject.getShortName();

    Map<String, List<String>> concepts = new HashMap<>();
    concepts.put(
        "OS",
        Arrays.asList(
            "process scheduling",
            "virtual memory",
            "file systems",
            "deadlocks",
            "memory management"));
    concepts.put(
        "AI",
        Arrays.asList(
            "neural networks",
            "expert systems",
            "genetic algorithms",
            "knowledge representation",
            "search algorithms"));
    concepts.put(
        "ML",
        Arrays.asList(
            "supervised learning",
            "unsupervised learning",
            "reinforcement learning",
            "feature extraction",
            "model evaluation"));
    concepts.put(
        "HCI",
        Arrays.asList(
            "usability testing",
            "interaction design",
            "cognitive models",
            "user research",
            "accessibility"));
    concepts.put(
        "SE",
        Arrays.asList(
            "agile methodology",
            "requirements gathering",
            "software testing",
            "version control",
            "continuous integration"));
    concepts.put(
        "CA",
        Arrays.asList(
            "processor architecture",
            "memory hierarchy",
            "instruction sets",
            "pipelining",
            "cache organization"));

    List<String> subjectConcepts =
        concepts.getOrDefault(
            shortName,
            Arrays.asList(
                "abstraction",
                "encapsulation",
                "modularity",
                "information hiding",
                "data structures"));

    return subjectConcepts.get(index % subjectConcepts.size());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createOptionsForQuestion(Question question, Subject subject, int questionIndex) {
    try {
      Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);

      String shortName = subject.getShortName();
      List<List<String>> options;

      int optionCount = 4;
      if (managedQuestion.getDifficulty() == QuestionDifficulty.EASY) {
        optionCount = 3;
      } else if (managedQuestion.getDifficulty() == QuestionDifficulty.HARD) {
        optionCount = 5;
      }

      switch (shortName) {
        case "PF", "OOP" -> {
          options = DataConstants.MULTIPLE_CHOICE_OPTIONS_JAVA;
          createProgrammingOptions(managedQuestion, options, questionIndex, optionCount);
        }
        case "DBS" -> {
          options = DataConstants.MULTIPLE_CHOICE_OPTIONS_DB;
          createProgrammingOptions(managedQuestion, options, questionIndex, optionCount);
        }
        case "CN", "NSEC" -> {
          options = DataConstants.MULTIPLE_CHOICE_OPTIONS_NETWORKS;
          createProgrammingOptions(managedQuestion, options, questionIndex, optionCount);
        }
        case "OS" -> {
          options = DataConstants.MULTIPLE_CHOICE_OPTIONS_OS;
          createProgrammingOptions(managedQuestion, options, questionIndex, optionCount);
        }
        case "AI", "ML", "CV", "NLP" -> {
          options = DataConstants.MULTIPLE_CHOICE_OPTIONS_AI;
          createProgrammingOptions(managedQuestion, options, questionIndex, optionCount);
        }
        default -> createGenericOptions(managedQuestion, optionCount);
      }
    } catch (Exception e) {
      log.error("Error creating options for question: {}", e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createProgrammingOptions(
      Question question, List<List<String>> options, int questionIndex, int optionCount) {
    try {
      Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);
      List<String> questionOptions = new ArrayList<>(options.get(questionIndex % options.size()));

      int actualOptionCount = Math.min(questionOptions.size(), optionCount);

      for (int i = 0; i < actualOptionCount; i++) {
        boolean isCorrect = i == 0;

        if (question.getDifficulty() == QuestionDifficulty.HARD
            && i == 0
            && random.nextDouble() < 0.3) {
          int swapIndex = 1 + random.nextInt(actualOptionCount - 1);
          String temp = questionOptions.getFirst();
          questionOptions.set(0, questionOptions.get(swapIndex));
          questionOptions.set(swapIndex, temp);
          isCorrect = false;
        }

        Option option =
            Option.builder()
                .text(questionOptions.get(i))
                .description("")
                .isCorrect(isCorrect)
                .question(managedQuestion)
                .build();

        optionRepository.save(option);
      }
    } catch (Exception e) {
      log.error("Error creating programming options: {}", e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createGenericOptions(Question question, int optionCount) {
    try {
      Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);

      String[] words = managedQuestion.getQuestionText().split("\\s+");
      String baseTerm = words.length > 3 ? words[3] : "concept";

      List<String> options = new ArrayList<>();
      QuestionDifficulty difficulty = managedQuestion.getDifficulty();

      switch (difficulty) {
        case QuestionDifficulty.EASY -> {
          options.add("The " + baseTerm + " is the main concept related to the question");
          options.add("The " + baseTerm + " is completely unrelated to the question");
          options.add("The " + baseTerm + " is optional and not important");
          options.add("The " + baseTerm + " only applies in theoretical situations");
        }
        case QuestionDifficulty.MEDIUM -> {
          options.add("The " + baseTerm + " represents the primary mechanism for data abstraction");
          options.add(
              "The " + baseTerm + " is an implementation detail not relevant to the interface");
          options.add("The " + baseTerm + " is a secondary component used only in specific cases");
          options.add(
              "The " + baseTerm + " is a theoretical concept with no practical applications");
          options.add(
              "The " + baseTerm + " is deprecated and should not be used in modern systems");
        }
        default -> {
          options.add(
              "The "
                  + baseTerm
                  + " provides an optimal solution by balancing time and space complexity");
          options.add(
              "The "
                  + baseTerm
                  + " offers better performance but significantly increases complexity");
          options.add(
              "The " + baseTerm + " is primarily useful for maintaining backward compatibility");
          options.add(
              "The " + baseTerm + " improves maintainability but introduces runtime overhead");
          options.add(
              "The "
                  + baseTerm
                  + " is considered best practice only in enterprise-scale applications");
          options.add(
              "The " + baseTerm + " is useful only when combined with other advanced techniques");
        }
      }

      for (int i = 0; i < Math.min(optionCount, options.size()); i++) {
        boolean isCorrect = i == 0;

        Option option =
            Option.builder()
                .text(options.get(i))
                .description("")
                .isCorrect(isCorrect)
                .question(managedQuestion)
                .build();

        optionRepository.save(option);
      }
    } catch (Exception e) {
      log.error("Error creating generic options: {}", e.getMessage());
    }
  }

  @Transactional
  public void createDefaultPrompt() {
    if (promptRepository.findById(1L).isPresent()) {
      log.info("Default grading prompt already exists");
      return;
    }

    try {
      User admin =
          baseAdmin != null
              ? baseAdmin
              : userRepository
                  .findByUsername(ADMIN_USERNAME)
                  .orElseThrow(() -> ResourceNotFoundException.user(ADMIN_USERNAME));

      ClassPathResource resource = new ClassPathResource("prompts/grading_prompt.txt");
      String promptContent;

      try {
        promptContent =
            new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      } catch (Exception e) {
        promptContent = DataConstants.PROMPT_TEMPLATES.getFirst();
      }

      Prompt defaultPrompt =
          Prompt.builder()
              .title("Default Grading Prompt")
              .description(
                  "Standard grading prompt for evaluating student submissions. This prompt provides guidelines for fair and constructive assessment.")
              .prompt(promptContent)
              .author(admin)
              .isPublic(true)
              .created(LocalDateTime.now())
              .build();

      promptRepository.save(defaultPrompt);
      log.info("Created default grading prompt successfully");
    } catch (Exception e) {
      log.error("Failed to create default grading prompt: {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void createAdditionalPrompts() {
    if (baseTeacher == null) {
      log.warn("Base teacher not found, skipping creating additional prompts");
      return;
    }

    int promptCount = (int) promptRepository.count();
    if (promptCount > 5) {
      log.info("Already have {} prompts, skipping creating additional prompts", promptCount);
      return;
    }

    List<User> teachers = userRepository.findAllByRole(RolesEnum.TEACHER);

    for (int i = 1; i < DataConstants.PROMPT_TEMPLATES.size(); i++) {
      String promptContent = DataConstants.PROMPT_TEMPLATES.get(i);

      User author = (i <= 2) ? baseTeacher : teachers.get(random.nextInt(teachers.size()));
      boolean isPublic = random.nextDouble() < 0.7;

      Prompt prompt =
          Prompt.builder()
              .title("Prompt " + (i + 1) + " - " + author.getUsername())
              .description(
                  "Custom evaluation prompt created by "
                      + author.getName()
                      + " "
                      + author.getSurname())
              .prompt(promptContent)
              .author(author)
              .isPublic(isPublic)
              .created(LocalDateTime.now())
              .build();

      promptRepository.save(prompt);
      log.info("Created prompt for {}: public={}", author.getUsername(), isPublic);
    }
  }

  private String generateGroupName(
      int index, Semester semester, int academicYear, int subjectGroupIndex, String subjectCode) {
    if (random.nextInt(4) == 0) {
      String semesterPrefix = semester == Semester.WINTER ? "W" : "S";
      String yearSuffix = String.valueOf(academicYear).substring(2);
      return String.format(
          "%s-%s%s-%d", subjectCode, semesterPrefix, yearSuffix, subjectGroupIndex + 1);
    } else if (random.nextInt(3) == 0) {
      String formatPattern =
          DataConstants.GROUP_NAME_FORMATS.get(index % DataConstants.GROUP_NAME_FORMATS.size());

      if (formatPattern.contains("%s")) {
        char suffix =
            DataConstants.GROUP_SUFFIXES.get(
                (index + subjectGroupIndex) % DataConstants.GROUP_SUFFIXES.size());

        if (formatPattern.equals("%sPRG%d")) {
          return String.format(formatPattern, suffix, academicYear % 10);
        } else {
          return String.format(formatPattern, (index % 5) + 1, suffix);
        }
      } else if (formatPattern.contains("%d%d%d")) {
        return String.format(formatPattern, (index % 3) + 1, (index % 2) + 1, (index % 5) + 1);
      } else if (formatPattern.contains("%d%d")) {
        return String.format(formatPattern, (index % 3) + 1, (index % 9) + 1);
      } else {
        return String.format(formatPattern, (index % 4) + 1, (index % 3) + 1);
      }
    } else {
      String semesterCode = semester == Semester.WINTER ? "W" : "S";
      int groupNum = subjectGroupIndex + 1;
      return String.format("%s-%s%d-%d", subjectCode, semesterCode, academicYear % 100, groupNum);
    }
  }
}
