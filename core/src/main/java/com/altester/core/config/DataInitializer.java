package com.altester.core.config;

import com.altester.core.serviceImpl.DataInit.DataInit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataInit dataInit;

    @Override
    public void run(String... args) {

        //Creating Students and Teachers
        dataInit.createStudents(100);
        dataInit.createTeachers(20);

        //Creating Subjects
        dataInit.createSubject(20);

        // Create groups with various configurations:
        // - Current semester (active) groups
        // - Past semester (inactive) groups
        // - Future semester groups
        // - Each with assigned tests, questions, and options
        dataInit.createStudentGroups();
    }

}
