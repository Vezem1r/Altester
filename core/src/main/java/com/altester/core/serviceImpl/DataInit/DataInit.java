package com.altester.core.serviceImpl.DataInit;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
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

    @Transactional
    protected void createSemesterGroups(Semester semester, int academicYear, int groupCount, boolean isActive, List<User> students, List<User> teachers) {

        log.info("Creating {} {} semester {} groups", groupCount, semester, academicYear);

        List<Subject> subjects = subjectRepository.findAll();
        if (subjects.isEmpty()) {
            log.warn("No subjects available to assign to groups.");
            return;
        }

        Map<Subject, Integer> subjectGroupCounts = new HashMap<>();

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

                for (int j = 0; j < studentCount && j < students.size(); j++) {
                    int studentIndex = (groupIndex * 7 + j * 3 + i * 11) % students.size();
                    groupStudents.add(students.get(studentIndex));
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

    @Transactional
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

            Test test = Test.builder()
                    .title(testTitle)
                    .description(description)
                    .duration(30 + (i * 15))
                    .isOpen(i % 2 == 0)
                    .maxAttempts(1 + i % 3)
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

    @Transactional
    protected void createQuestionsForTest(Test test, Subject subject, int questionMultiplier) {
        int baseQuestionCount = 5 + questionMultiplier;
        int maxExtraQuestions = 5 + questionMultiplier;
        int questionCount = baseQuestionCount + random.nextInt(maxExtraQuestions);

        for (int i = 0; i < questionCount; i++) {
            QuestionType questionType = random.nextBoolean() ?
                    QuestionType.MULTIPLE_CHOICE : QuestionType.TEXT_ONLY;

            String questionText = getQuestionText(subject, i);

            int baseScore = 5 + (i % 5) * 2;

            Test managedTest = testRepository.findById(test.getId()).orElse(test);

            Question question = Question.builder()
                    .questionText(questionText)
                    .imagePath(null)
                    .score(baseScore)
                    .questionType(questionType)
                    .test(managedTest)
                    .position(i + 1)
                    .build();

            Question savedQuestion = questionRepository.save(question);

            if (questionType == QuestionType.MULTIPLE_CHOICE) {
                createOptionsForQuestion(savedQuestion, subject, i);
            }

            log.info("Created {} question for test '{}'",
                    questionType,
                    managedTest.getTitle());
        }
    }

    private String getQuestionText(Subject subject, int index) {
        String shortName = subject.getShortName();
        List<String> questions;

        switch (shortName) {
            case "PF", "DSA", "OOP" -> questions = DataConstants.CODING_QUESTIONS;
            case "DBS" -> questions = DataConstants.DATABASE_QUESTIONS;
            case "CN" -> questions = DataConstants.NETWORKING_QUESTIONS;
            default -> {
                return "Explain the concept of " + getConceptForSubject(subject, index);
            }
        }

        return questions.get(index % questions.size());
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

    @Transactional
    protected void createOptionsForQuestion(Question question, Subject subject, int questionIndex) {
        String shortName = subject.getShortName();
        List<List<String>> options;

        if (shortName.equals("PF") || shortName.equals("OOP")) {
            options = DataConstants.MULTIPLE_CHOICE_OPTIONS_JAVA;
        } else if (shortName.equals("DBS")) {
            options = DataConstants.MULTIPLE_CHOICE_OPTIONS_DB;
        } else {
            createGenericOptions(question, 4);
            return;
        }

        Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);
        List<String> questionOptions = options.get(questionIndex % options.size());

        for (int i = 0; i < questionOptions.size(); i++) {
            boolean isCorrect = i == 0;

            Option option = Option.builder()
                    .text(questionOptions.get(i))
                    .description("")
                    .isCorrect(isCorrect)
                    .question(managedQuestion)
                    .build();

            optionRepository.save(option);
        }
    }

    @Transactional
    protected void createGenericOptions(Question question, int optionCount) {
        String[] words = question.getQuestionText().split("\\s+");
        String baseTerm = words.length > 3 ? words[3] : "concept";

        List<String> options = new ArrayList<>();

        options.add("The " + baseTerm + " represents the primary mechanism for data abstraction");
        options.add("The " + baseTerm + " is an implementation detail not relevant to the interface");
        options.add("The " + baseTerm + " is a secondary component used only in specific cases");
        options.add("The " + baseTerm + " is a theoretical concept with no practical applications");

        Question managedQuestion = questionRepository.findById(question.getId()).orElse(question);

        for (int i = 0; i < optionCount && i < options.size(); i++) {
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