package com.altester.core.config;

import com.altester.core.service.DataInit.DataInit;
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
    public void run(String... args) throws Exception {

        //Creating Students and Teachers
        dataInit.createStudents(50);
        dataInit.createTeachers(10);

        //Creating Subjects
        dataInit.createSubject(10);
    }

}
