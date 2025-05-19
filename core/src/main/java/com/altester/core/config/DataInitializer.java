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
    log.info("Starting data initialization process...");

    // Create default admin, student, and teacher users
    dataInit.createDefaultUsers();

    // Creating Students and Teachers
    dataInit.createStudents(200);
    dataInit.createTeachers(45);

    // Creating Subjects
    dataInit.createSubject(30);

    // Create groups with various configurations:
    // - Multiple groups per subject in current semester/year
    // - Past semester groups
    // - Future semester groups
    // - Each with assigned tests, questions, and options
    // This also handles:
    // - Assigning base teacher to 4 current, 2 past, 2 future groups
    // - Adding base student to 5-6 different groups
    // - Creating tests with different difficulty questions
    // - Creating attempts and submissions for students
    dataInit.createStudentGroups();

    // Creates default prompt for fallback
    dataInit.createDefaultPrompt();

    // Additional prompts are created automatically as part of createStudentGroups
    dataInit.createAdditionalPrompts();

    log.info("Data initialization completed successfully");
  }
}
