package com.altester.core.service.DataInit;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final SemesterConfig semesterConfig;

    @Value("${admin.password}")
    private String password;

    List<String> surnames = new ArrayList<>(Arrays.asList(
            "Novak", "Svoboda", "Dvorak", "Kucera", "Hajek", "Novotny", "Jelinek", "Vavra", "Dolezal", "Kratochvil",
            "Cerny", "Polak", "Kolar", "Zeman", "Kucera", "Fiala", "Havel", "Rehak", "Chlapek", "Jirasek",
            "Kral", "Urban", "Horak", "Novakova", "Tomek", "Kubik", "Houska", "Kott", "Horky", "Karas",
            "Bartoš", "Kalous", "Vacek", "Šimánek", "Pospisil", "Křen", "Dvořáková", "Michal", "Vesely",
            "Stepanek", "Dlouhy", "Prouza", "Sykora", "Čech", "Janda", "Kubát", "Kohout", "Pokorny",
            "Havelka", "Kunc", "Růžička", "Kříž", "Hrbek", "Zavadil", "Svoboda", "Toman", "Tůma", "Pojar",
            "Blaha", "Cerný", "Dušek", "Kozák", "Rajchert", "Pex", "Langer", "Beneš", "Chrást", "Mlynář",
            "Hrubý", "Krejčí", "Suchánek", "Jelínek", "Bauman", "Volný", "Hřebíček", "Dobrý", "Dědič",
            "Šťastný", "Hrubeš", "Patek", "Bažant", "Novotná", "Cejpek", "Sladký", "Mrázek", "Semerád",
            "Hanák", "Smrčka", "Knížek", "Pabian", "Vlk", "Černoch", "Schubert", "Řehák", "Fiala",
            "Doležalová", "Králová", "Straka", "Kolář", "Cikán"
    ));

    List<String> firstnames = Arrays.asList(
            "Jan", "Petr", "Lukas", "Tomas", "Martin", "Jakub", "Filip", "David", "Jiri", "Michal",
            "Roman", "Martin", "Vojtech", "Karel", "Pavel", "Radek", "Marek", "Jaroslav", "Ondrej", "Ladislav",
            "Josef", "Frantisek", "Adam", "Vladimir", "Stanislav", "Rostislav", "Jindrich", "Dominik", "Miroslav",
            "Robert", "Daniel", "Tomasz", "Patrik", "Milan", "Sven", "Alexander", "David", "Benjamin", "Petr",
            "Kamil", "Jozef", "Lukas", "Martin", "Igor", "Jakub", "Janek", "Dalibor", "Janusz", "Tibor",
            "Vít", "Marek", "Kamil", "Daniel", "Janek", "Vlastimil", "Michal", "Martin", "Stanislav", "Roman",
            "Lubomir", "Jiri", "Zdenek", "Petr", "Adrian", "Jindrich", "Adam", "Frantisek", "Alfred",
            "Libor", "Miloslav", "Dusan", "Roman", "Bohuslav", "Hugo", "Marian", "František", "Šimon",
            "Ladislav", "Norbert", "Václav", "Leoš", "Bohumil", "Karel", "Zbyněk", "Emanuel", "Dalibor",
            "Tibor", "Jirka", "Zdenek", "Kamil", "Jakub", "Emil", "František", "Eduard", "Jaromír",
            "Radoslav", "Dalibor", "Jiří", "Otakar", "René", "Igor", "Jan", "Petr", "Jarek", "Vojtěch"
    );

    List<String> subjects = Arrays.asList(
            "Zaklady Pocitacove grafiky", "Operacni systemy", "Programovani v jazyce Java", "Matematika pro informatiky",
            "Databazove systemy", "Inzenyrska etika", "Vyvoj mobilnich aplikaci", "Počítačová síť", "Teorie algoritmů",
            "Fyzika pro informatiku", "Analýza a návrh systémů", "Softwarové inženýrství", "Statistika", "Kreativní psaní",
            "Umělá inteligence", "Počítačová grafika", "Bezpečnost počítačových sítí", "Testování a validace software",
            "Správa a konfigurace systémů", "Počítačová lingvistika"
    );

    List<String> shortNames = Arrays.asList(
            "ZPG", "OSY", "PJV", "MPI", "DSY", "IE", "VMA", "PCS", "TA", "FP",
            "ANS", "SI", "ST", "KP", "UI", "PG", "BPS", "TS", "SCS", "PCL"
    );


    public void createStudents(int amount) {

        if (amount > 100) {
            amount = 100;
        }

        log.info("Creating {} students", amount);

        for (int i = 0; i < amount; i++) {
            String firstname = firstnames.get(i);
            String lastname = surnames.get(i);
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

    private void createDefaultUsers() {
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
                    .name("teacher")
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
                    .name("admin")
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

    public void createTeachers(int amount) {

        if (amount > 20) {
            amount = 20;
        }

        log.info("Creating {} teachers", amount);

        for (int i = 0; i < amount; i++) {
            String firstname = firstnames.get(i);
            String lastname = surnames.get(i);
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
        Random random = new Random();
        String prefix = surname.substring(0, 3).toUpperCase();
        log.info("Generating username with prefix {}", prefix);

        String username;
        boolean isUnique;

        do {
            int randomNumber = 100 + random.nextInt(900);
            username = String.format("%sR%03d", prefix, randomNumber);
            isUnique = userRepository.findByUsername(username).isEmpty();
        } while (!isUnique);

        return username;
    }

    public void createSubject(int amount) {

        if (amount > 20) {
            amount = 20;
        }

        log.info("Creating {} subjects", amount);

        for (int i = 0; i < amount; i++) {
            String subjectName = subjects.get(i);
            String shortName = shortNames.get(i);
            String description = "This is subject with name " + (subjectName) + " and its shortname is " + shortName;

            if (subjectRepository.findByShortName(shortName).isPresent()) {
                log.info("Skipping existing subject: {}", shortName);
                continue;
            }

            Subject subject = Subject.builder()
                    .name(subjectName)
                    .shortName(shortName)
                    .description(description)
                    .build();

            subjectRepository.save(subject);
            log.info("Created subject: {} {}", subjectName, shortName);
        }
    }

    public void createStudentGroups() {
        List<User> students = userRepository.findByRole(RolesEnum.STUDENT, Pageable.unpaged()).getContent();

        if (students.isEmpty()) {
            log.warn("No students available to create groups.");
            return;
        }

        User teacher = userRepository.findByUsername("TEACHER")
                .orElseThrow(() -> new IllegalArgumentException("Teacher cannot be null"));

        int totalGroups = (int) Math.ceil((double) students.size() / 10);

        for (int i = 0; i < totalGroups; i++) {
            int startIndex = i * 10;
            int endIndex = Math.min((i + 1) * 10, students.size());
            List<User> groupStudents = students.subList(startIndex, endIndex);

            Set<User> studentSet = new HashSet<>(groupStudents);

            String groupName = "Group_" + (i + 1);

            Optional<Group> optionalGroup = groupRepository.findByName(groupName);
            if (optionalGroup.isPresent()) {
                log.info("Skipping existing group: {}", groupName);
                continue;
            }

            Semester semester = Semester.valueOf(semesterConfig.getCurrentSemester());
            int academicYear = semesterConfig.getCurrentAcademicYear();
            boolean isActive = semesterConfig.isSemesterActive(semester.name(), academicYear);

            Group group = Group.builder()
                    .name(groupName)
                    .teacher(teacher)
                    .students(studentSet)
                    .semester(semester)
                    .academicYear(academicYear)
                    .active(isActive)
                    .build();

            groupRepository.save(group);
            log.info("Created group: {} with {} students", groupName, studentSet.size());
        }
    }
}
