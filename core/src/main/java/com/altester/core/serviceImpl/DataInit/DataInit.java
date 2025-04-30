package com.altester.core.serviceImpl.DataInit;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.model.subject.enums.QuestionType;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.OptionRepository;
import com.altester.core.repository.QuestionRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final Random random = new Random();

    @Value("${admin.password}")
    private String password;

    @Transactional
    public void createStudents(int amount) {
        if (amount > DataConstants.SURNAMES.size()) {
            amount = DataConstants.SURNAMES.size();
        }

        log.info("Creating {} students", amount);

        for (int i = 0; i < amount; i++) {
            String firstname = DataConstants.FIRSTNAMES.get(i);
            String lastname = DataConstants.SURNAMES.get(i);
            String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + ".st@vsb.cz";

            if (userRepository.findByEmail(email).isPresent()) {
                log.info("Skipping existing student: {}", email);
                continue;
            }

            String username = generateUsername(lastname);
            String pass = passwordEncoder.encode(password);

            User user = User.builder()
                    .name(firstname)
                    .surname(lastname)
                    .email(email)
                    .username(username)
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .password(pass)
                    .enabled(true)
                    .isRegistered(true)
                    .role(RolesEnum.STUDENT)
                    .build();

            userRepository.save(user);
            log.info("Created student: {} {}", firstname, lastname);
        }

        createDefaultUsers();
    }

    @Transactional
    protected void createDefaultUsers() {
        if (userRepository.findByUsername("STUDENT").isEmpty()) {
            String pass = passwordEncoder.encode("1234");
            User student = User.builder()
                    .name("Student")
                    .surname("Super")
                    .email("student@vsb.cz")
                    .username("STUDENT")
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .password(pass)
                    .enabled(true)
                    .isRegistered(true)
                    .role(RolesEnum.STUDENT)
                    .build();
            userRepository.save(student);
        }

        if (userRepository.findByUsername("TEACHER").isEmpty()) {
            String pass = passwordEncoder.encode("1234");
            User teacher = User.builder()
                    .name("Teacher")
                    .surname("Super")
                    .email("teacher@vsb.cz")
                    .username("TEACHER")
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .password(pass)
                    .enabled(true)
                    .isRegistered(true)
                    .role(RolesEnum.TEACHER)
                    .build();
            userRepository.save(teacher);
        }

        if (userRepository.findByUsername("ADMIN").isEmpty()) {
            String pass = passwordEncoder.encode("1234");
            User admin = User.builder()
                    .name("Admin")
                    .surname("Super")
                    .email("admin@vsb.cz")
                    .username("ADMIN")
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .password(pass)
                    .enabled(true)
                    .isRegistered(true)
                    .role(RolesEnum.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }

    @Transactional
    public void createTeachers(int amount) {
        if (amount > 20) {
            amount = 20;
        }

        log.info("Creating {} teachers", amount);

        for (int i = 0; i < amount; i++) {
            String firstname = DataConstants.FIRSTNAMES.get(i + 30);
            String lastname = DataConstants.SURNAMES.get(i + 30);
            String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@vsb.cz";

            if (userRepository.findByEmail(email).isPresent()) {
                log.info("Skipping existing teacher: {}", email);
                continue;
            }

            String username = generateUsername(lastname);
            String pass = passwordEncoder.encode(password);

            User user = User.builder()
                    .name(firstname)
                    .surname(lastname)
                    .email(email)
                    .username(username)
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
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
            String description = "This course covers " + subjectName +
                    " concepts and practical applications. Students will learn fundamental principles " +
                    "and develop skills through hands-on exercises and projects.";

            if (subjectRepository.findByShortName(shortName).isPresent()) {
                log.info("Skipping existing subject: {}", shortName);
                continue;
            }

            Subject subject = Subject.builder()
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
        List<User> students = userRepository.findByRole(RolesEnum.STUDENT, Pageable.unpaged()).getContent();
        List<User> teachers = userRepository.findByRole(RolesEnum.TEACHER, Pageable.unpaged()).getContent();

        if (students.isEmpty()) {
            log.warn("No students available to create groups.");
            return;
        }

        if (teachers.isEmpty()) {
            log.warn("No teachers available to create groups.");
            return;
        }

        String currentSemester = semesterConfig.getCurrentSemester();
        int currentYear = semesterConfig.getCurrentAcademicYear();

        createSemesterGroups(Semester.valueOf(currentSemester), currentYear, 25, true, students, teachers);

        Semester previousSemester = (Semester.valueOf(currentSemester) == Semester.WINTER) ?
                Semester.SUMMER : Semester.WINTER;
        int previousYear = (previousSemester == Semester.SUMMER) ?
                currentYear - 1 : currentYear;
        createSemesterGroups(previousSemester, previousYear, 15, false, students, teachers);

        Semester twoSemestersAgo = (previousSemester == Semester.WINTER) ?
                Semester.SUMMER : Semester.WINTER;
        int twoSemestersAgoYear = (twoSemestersAgo == Semester.SUMMER) ?
                previousYear - 1 : previousYear;
        createSemesterGroups(twoSemestersAgo, twoSemestersAgoYear, 10, false, students, teachers);

        Semester nextSemester = (Semester.valueOf(currentSemester) == Semester.WINTER) ?
                Semester.SUMMER : Semester.WINTER;
        int nextYear = (nextSemester == Semester.WINTER) ?
                currentYear + 1 : currentYear;
        createSemesterGroups(nextSemester, nextYear, 12, false, students, teachers);

        Semester twoSemestersAhead = (nextSemester == Semester.WINTER) ?
                Semester.SUMMER : Semester.WINTER;
        int twoSemestersAheadYear = (twoSemestersAhead == Semester.SUMMER) ?
                nextYear : nextYear + 1;
        createSemesterGroups(twoSemestersAhead, twoSemestersAheadYear, 8, false, students, teachers);
    }

    protected void createSemesterGroups(Semester semester, int academicYear, int groupCount, boolean isActive, List<User> students, List<User> teachers) {
        log.info("Creating {} {} semester {} groups", groupCount, semester, academicYear);

        List<Subject> subjects = subjectRepository.findAll();
        if (subjects.isEmpty()) {
            log.warn("No subjects available to assign to groups.");
            return;
        }

        Map<Subject, Integer> subjectGroupCounts = new HashMap<>();

        Map<Subject, Set<User>> studentsAssignedToSubject = new HashMap<>();

        for (Subject subject : subjects) {
            studentsAssignedToSubject.put(subject, new HashSet<>());
        }

        int remainingGroups = groupCount;
        int subjectsWithGroups = Math.min(subjects.size(), groupCount);

        for (int i = 0; i < subjectsWithGroups; i++) {
            subjectGroupCounts.put(subjects.get(i), 1);
            remainingGroups--;
        }

        while (remainingGroups > 0) {
            int prioritySubjectsCount = Math.max(1, subjectsWithGroups / 3);

            for (int i = 0; i < prioritySubjectsCount && remainingGroups > 0; i++) {
                Subject subject = subjects.get(i % subjectsWithGroups);
                subjectGroupCounts.put(subject, subjectGroupCounts.get(subject) + 1);
                remainingGroups--;
            }
        }

        int groupIndex = 0;
        for (Map.Entry<Subject, Integer> entry : subjectGroupCounts.entrySet()) {
            Subject subject = entry.getKey();
            int groupsForSubject = entry.getValue();

            for (int i = 0; i < groupsForSubject; i++) {
                String groupName = generateGroupName(groupIndex, semester, academicYear, i, subject.getShortName());

                if (groupRepository.findByName(groupName).isPresent()) {
                    log.info("Skipping existing group: {}", groupName);
                    continue;
                }

                User teacher = teachers.get((int)(subject.getId() % teachers.size() + i) % teachers.size());

                int studentCount = 5 + random.nextInt(11);
                Set<User> groupStudents = new HashSet<>();

                int attemptsCount = 0;
                int maxAttempts = students.size() * 2;

                while (groupStudents.size() < studentCount && attemptsCount < maxAttempts) {
                    int studentIndex = (groupIndex * 7 + attemptsCount * 3 + i * 11) % students.size();
                    User student = students.get(studentIndex);

                    Set<User> assignedStudents = studentsAssignedToSubject.computeIfAbsent(subject, k -> new HashSet<>());

                    if (!assignedStudents.contains(student)) {
                        groupStudents.add(student);
                        assignedStudents.add(student);
                    }

                    attemptsCount++;
                }

                boolean groupIsActive = isActive;
                if (groupActivityService.isGroupInFuture(Group.builder()
                        .semester(semester)
                        .academicYear(academicYear)
                        .build())) {
                    groupIsActive = false;
                }

                Group group = Group.builder()
                        .name(groupName)
                        .teacher(teacher)
                        .students(groupStudents)
                        .semester(semester)
                        .academicYear(academicYear)
                        .active(groupIsActive)
                        .tests(new HashSet<>())
                        .build();

                Group savedGroup = groupRepository.save(group);
                log.info("Created group: {} with {} students for {}/{} - {}",
                        groupName, groupStudents.size(), semester, academicYear,
                        groupIsActive ? "active" : "inactive");

                Subject managedSubject = subjectRepository.findById(subject.getId()).orElse(subject);

                if (managedSubject.getGroups() == null) {
                    managedSubject.setGroups(new HashSet<>());
                }
                managedSubject.getGroups().add(savedGroup);
                subjectRepository.save(managedSubject);
                log.info("Assigned group {} to subject {}", groupName, managedSubject.getShortName());

                createTestsForGroup(savedGroup, managedSubject);

                groupIndex++;
            }
        }
    }

    private String generateGroupName(int index, Semester semester, int academicYear, int subjectGroupIndex, String subjectCode) {
        if (random.nextInt(4) == 0) {
            String semesterPrefix = semester == Semester.WINTER ? "W" : "S";
            String yearSuffix = String.valueOf(academicYear).substring(2);
            return String.format("%s-%s%s-%d", subjectCode, semesterPrefix, yearSuffix, subjectGroupIndex + 1);
        } else if (random.nextInt(3) == 0) {
            String formatPattern = DataConstants.GROUP_NAME_FORMATS.get(index % DataConstants.GROUP_NAME_FORMATS.size());

            if (formatPattern.contains("%s")) {
                char suffix = DataConstants.GROUP_SUFFIXES.get((index + subjectGroupIndex) % DataConstants.GROUP_SUFFIXES.size());

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

    protected void createTestsForGroup(Group group, Subject subject) {
        int baseTestCount = group.isActive() ? 3 : 2;
        int variance = group.isActive() ? 3 : 1;
        int testCount = baseTestCount + random.nextInt(variance);

        for (int i = 0; i < testCount; i++) {
            String testTitle = getTestTitleForSubject(subject, i);
            String description = DataConstants.TEST_DESCRIPTIONS.get(i % DataConstants.TEST_DESCRIPTIONS.size());

            boolean createdByAdmin = i % 3 == 0;
            boolean allowTeacherEdit = i % 3 == 1 || !createdByAdmin;

            LocalDateTime startTime, endTime;
            if (!group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                startTime = LocalDateTime.now().minusMonths(3).plusDays(i * 3L);
                endTime = startTime.plusDays(14);
            }
            else if (groupActivityService.isGroupInFuture(group)) {
                startTime = LocalDateTime.now().plusMonths(1).plusDays(i * 3L);
                endTime = startTime.plusDays(14);
            }
            else {
                if (i % 3 == 0) {
                    startTime = LocalDateTime.now().minusDays(30 + random.nextInt(30));
                    endTime = startTime.plusDays(7);
                } else if (i % 3 == 1) {
                    startTime = LocalDateTime.now().minusDays(3);
                    endTime = LocalDateTime.now().plusDays(4);
                } else {
                    startTime = LocalDateTime.now().plusDays(7 + random.nextInt(21));
                    endTime = startTime.plusDays(7);
                }
            }

            int easyCount = 2 + random.nextInt(3);
            int mediumCount = 3 + random.nextInt(3);
            int hardCount = 1 + random.nextInt(2);

            if (i % 3 == 2) {
                easyCount += 1;
                mediumCount += 2;
                hardCount += 1;
            }

            Test test = Test.builder()
                    .title(testTitle)
                    .description(description)
                    .duration(30 + (i * 15))
                    .isOpen(group.isActive() && i % 2 == 0)
                    .maxAttempts(1 + i % 3)
                    .easyQuestionsCount(easyCount)
                    .mediumQuestionsCount(mediumCount)
                    .hardQuestionsCount(hardCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .isCreatedByAdmin(createdByAdmin)
                    .allowTeacherEdit(allowTeacherEdit)
                    .build();

            Test savedTest = testRepository.save(test);

            Group managedGroup = groupRepository.findById(group.getId()).orElse(group);
            if (managedGroup.getTests() == null) {
                managedGroup.setTests(new HashSet<>());
            }
            managedGroup.getTests().add(savedTest);
            groupRepository.save(managedGroup);

            int questionMultiplier = group.isActive() ? 1 : 0;
            createQuestionsForTest(savedTest, subject, questionMultiplier);

            log.info("Created test '{}' for group {} - {} ({})",
                    testTitle,
                    managedGroup.getName(),
                    createdByAdmin ? "admin created" : "teacher created",
                    group.isActive() ? "active" : (groupActivityService.isGroupInFuture(group) ? "future" : "past"));
        }
    }

    private String getTestTitleForSubject(Subject subject, int index) {
        String shortName = subject.getShortName();
        List<String> testTitles;

        switch (shortName) {
            case "PF":
            case "DSA":
            case "OOP":
                testTitles = DataConstants.TEST_TITLES_PROGRAMMING;
                break;
            case "DBS":
                testTitles = DataConstants.TEST_TITLES_DATABASES;
                break;
            case "CN":
                testTitles = DataConstants.TEST_TITLES_NETWORKS;
                break;
            case "OS":
                testTitles = DataConstants.TEST_TITLES_OS;
                break;
            case "AI":
            case "ML":
                testTitles = DataConstants.TEST_TITLES_AI;
                break;
            case "SE":
            case "WD":
                testTitles = DataConstants.TEST_TITLES_SE;
                break;
            default:
                return subject.getShortName() + " - Test " + (index + 1);
        }

        return subject.getShortName() + ": " + testTitles.get(index % testTitles.size());
    }

    protected void createQuestionsForTest(Test test, Subject subject, int questionMultiplier) {
        int easyQuestionsToCreate = Math.max(test.getEasyQuestionsCount() + 1, 3);
        int mediumQuestionsToCreate = Math.max(test.getMediumQuestionsCount() + 2, 4);
        int hardQuestionsToCreate = Math.max(test.getHardQuestionsCount() + 1, 2);

        if (questionMultiplier > 0) {
            easyQuestionsToCreate += random.nextInt(3);
            mediumQuestionsToCreate += random.nextInt(4);
            hardQuestionsToCreate += random.nextInt(2);
        }

        int easyQuestionScore = 2 + random.nextInt(2);
        int mediumQuestionScore = 4 + random.nextInt(3);
        int hardQuestionScore = 7 + random.nextInt(4);

        Test managedTest = testRepository.findById(test.getId()).orElse(test);

        createQuestionsWithDifficulty(managedTest, subject, easyQuestionsToCreate, QuestionDifficulty.EASY, easyQuestionScore);
        createQuestionsWithDifficulty(managedTest, subject, mediumQuestionsToCreate, QuestionDifficulty.MEDIUM, mediumQuestionScore);
        createQuestionsWithDifficulty(managedTest, subject, hardQuestionsToCreate, QuestionDifficulty.HARD, hardQuestionScore);

        log.info("Created {} questions for test '{}' ({} easy @ {} pts, {} medium @ {} pts, {} hard @ {} pts)",
                easyQuestionsToCreate + mediumQuestionsToCreate + hardQuestionsToCreate,
                managedTest.getTitle(),
                easyQuestionsToCreate, easyQuestionScore,
                mediumQuestionsToCreate, mediumQuestionScore,
                hardQuestionsToCreate, hardQuestionScore);
    }

    private void createQuestionsWithDifficulty(Test test, Subject subject, int count, QuestionDifficulty difficulty, int score) {
        for (int i = 0; i < count; i++) {
            QuestionType questionType;

            if (difficulty == QuestionDifficulty.EASY) {
                questionType = random.nextDouble() < 0.7 ? QuestionType.MULTIPLE_CHOICE : QuestionType.TEXT_ONLY;
            } else if (difficulty == QuestionDifficulty.MEDIUM) {
                questionType = random.nextDouble() < 0.5 ? QuestionType.MULTIPLE_CHOICE : QuestionType.TEXT_ONLY;
            } else {
                questionType = random.nextDouble() < 0.3 ? QuestionType.MULTIPLE_CHOICE : QuestionType.TEXT_ONLY;
            }

            String questionText = getQuestionTextForDifficulty(subject, i, difficulty);

            Question question = Question.builder()
                    .questionText(questionText)
                    .imagePath(null)
                    .score(score)
                    .questionType(questionType)
                    .difficulty(difficulty)
                    .test(test)
                    .build();

            Question savedQuestion = questionRepository.save(question);

            if (questionType == QuestionType.MULTIPLE_CHOICE) {
                createOptionsForQuestion(savedQuestion, subject, i);
            }

            log.debug("Created {} {} question for test '{}'",
                    difficulty, questionType, test.getTitle());
        }
    }

    private String getQuestionTextForDifficulty(Subject subject, int index, QuestionDifficulty difficulty) {
        String shortName = subject.getShortName();
        String difficultyPrefix = switch (difficulty) {
            case EASY -> "Define";
            case MEDIUM -> "Explain";
            case HARD -> "Analyze";
        };

        List<String> questions;

        switch (shortName) {
            case "PF", "DSA", "OOP" -> {
                questions = DataConstants.CODING_QUESTIONS;
                String baseQuestion = questions.get(index % questions.size());
                if (difficulty == QuestionDifficulty.EASY) {
                    return "Basic: " + baseQuestion;
                } else if (difficulty == QuestionDifficulty.MEDIUM) {
                    return "Intermediate: " + baseQuestion;
                } else {
                    return "Advanced: " + baseQuestion;
                }
            }
            case "DBS" -> {
                questions = DataConstants.DATABASE_QUESTIONS;
                String baseQuestion = questions.get(index % questions.size());
                return difficultyPrefix + " - " + baseQuestion;
            }
            case "CN" -> {
                questions = DataConstants.NETWORKING_QUESTIONS;
                String baseQuestion = questions.get(index % questions.size());
                return difficultyPrefix + " - " + baseQuestion;
            }
            default -> {
                return difficultyPrefix + " the concept of " + getConceptForSubject(subject, index);
            }
        }
    }

    private String getQuestionText(Subject subject, int index) {
        return getQuestionTextForDifficulty(subject, index, QuestionDifficulty.MEDIUM);
    }

    private String getConceptForSubject(Subject subject, int index) {
        String shortName = subject.getShortName();

        Map<String, List<String>> concepts = new HashMap<>();
        concepts.put("OS", Arrays.asList("process scheduling", "virtual memory", "file systems", "deadlocks", "memory management"));
        concepts.put("AI", Arrays.asList("neural networks", "expert systems", "genetic algorithms", "knowledge representation", "search algorithms"));
        concepts.put("ML", Arrays.asList("supervised learning", "unsupervised learning", "reinforcement learning", "feature extraction", "model evaluation"));
        concepts.put("HCI", Arrays.asList("usability testing", "interaction design", "cognitive models", "user research", "accessibility"));
        concepts.put("SE", Arrays.asList("agile methodology", "requirements gathering", "software testing", "version control", "continuous integration"));
        concepts.put("CA", Arrays.asList("processor architecture", "memory hierarchy", "instruction sets", "pipelining", "cache organization"));

        List<String> subjectConcepts = concepts.getOrDefault(shortName,
                Arrays.asList("abstraction", "encapsulation", "modularity", "information hiding", "data structures"));

        return subjectConcepts.get(index % subjectConcepts.size());
    }

    protected void createOptionsForQuestion(Question question, Subject subject, int questionIndex) {
        String shortName = subject.getShortName();
        List<List<String>> options;

        int optionCount = 4;
        if (question.getDifficulty() == QuestionDifficulty.EASY) {
            optionCount = 3;
        } else if (question.getDifficulty() == QuestionDifficulty.HARD) {
            optionCount = 5;
        }

        if (shortName.equals("PF") || shortName.equals("OOP")) {
            options = DataConstants.MULTIPLE_CHOICE_OPTIONS_JAVA;
            createProgrammingOptions(question, options, questionIndex, optionCount);
        } else if (shortName.equals("DBS")) {
            options = DataConstants.MULTIPLE_CHOICE_OPTIONS_DB;
            createProgrammingOptions(question, options, questionIndex, optionCount);
        } else {
            createGenericOptions(question, optionCount);
        }
    }

    private void createProgrammingOptions(Question question, List<List<String>> options, int questionIndex, int optionCount) {
        Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);
        List<String> questionOptions = options.get(questionIndex % options.size());

        int actualOptionCount = Math.min(questionOptions.size(), optionCount);

        for (int i = 0; i < actualOptionCount; i++) {
            boolean isCorrect = i == 0;

            if (question.getDifficulty() == QuestionDifficulty.HARD && i == 0 && random.nextDouble() < 0.3) {
                int swapIndex = 1 + random.nextInt(actualOptionCount - 1);
                String temp = questionOptions.getFirst();
                questionOptions.set(0, questionOptions.get(swapIndex));
                questionOptions.set(swapIndex, temp);
                isCorrect = false;
            }

            Option option = Option.builder()
                    .text(questionOptions.get(i))
                    .description("")
                    .isCorrect(isCorrect)
                    .question(managedQuestion)
                    .build();

            optionRepository.save(option);
        }
    }

    protected void createGenericOptions(Question question, int optionCount) {
        String[] words = question.getQuestionText().split("\\s+");
        String baseTerm = words.length > 3 ? words[3] : "concept";

        List<String> options = new ArrayList<>();
        QuestionDifficulty difficulty = question.getDifficulty();

        if (difficulty == QuestionDifficulty.EASY) {
            options.add("The " + baseTerm + " is the main concept related to the question");
            options.add("The " + baseTerm + " is completely unrelated to the question");
            options.add("The " + baseTerm + " is optional and not important");
            options.add("The " + baseTerm + " only applies in theoretical situations");
        } else if (difficulty == QuestionDifficulty.MEDIUM) {
            options.add("The " + baseTerm + " represents the primary mechanism for data abstraction");
            options.add("The " + baseTerm + " is an implementation detail not relevant to the interface");
            options.add("The " + baseTerm + " is a secondary component used only in specific cases");
            options.add("The " + baseTerm + " is a theoretical concept with no practical applications");
            options.add("The " + baseTerm + " is deprecated and should not be used in modern systems");
        } else {
            options.add("The " + baseTerm + " provides an optimal solution by balancing time and space complexity");
            options.add("The " + baseTerm + " offers better performance but significantly increases complexity");
            options.add("The " + baseTerm + " is primarily useful for maintaining backward compatibility");
            options.add("The " + baseTerm + " improves maintainability but introduces runtime overhead");
            options.add("The " + baseTerm + " is considered best practice only in enterprise-scale applications");
            options.add("The " + baseTerm + " is useful only when combined with other advanced techniques");
        }

        Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);

        for (int i = 0; i < Math.min(optionCount, options.size()); i++) {
            boolean isCorrect = i == 0;

            Option option = Option.builder()
                    .text(options.get(i))
                    .description("")
                    .isCorrect(isCorrect)
                    .question(managedQuestion)
                    .build();

            optionRepository.save(option);
        }
    }
}