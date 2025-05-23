package com.altester.core.config;

import com.altester.core.serviceImpl.DataInit.DataInit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final DataInit dataInit;

  @Value("${data.init.enabled:false}")
  private boolean dataInitEnabled;

  @Value("${data.init.check.existing:true}")
  private boolean checkExisting;

  @Override
  public void run(String... args) {
    if (!dataInitEnabled) {
      log.info("Data initialization is disabled. Set data.init.enabled=true to enable.");
      return;
    }

    log.info("Starting data initialization process...");

    try {
      // Check if data already exists
      if (checkExisting && dataInit.isDataAlreadyInitialized()) {
        log.info("Data already initialized. Skipping data initialization.");
        return;
      }

      // Phase 1: Create default admin (always if not exists)
      dataInit.createDefaultAdmin();

      // Phase 2: Create base users (student and teacher)
      dataInit.createBaseUsers();

      // Phase 3: Create bulk users
      dataInit.createStudents(200);
      dataInit.createTeachers(50);

      // Phase 4: Create IT subjects
      dataInit.createITSubjects();

      // Phase 5: Create groups for subjects
      dataInit.createGroupsForAllSubjects();

      // Phase 6: Create special groups for base teacher
      dataInit.createSpecialGroupsForBaseTeacher();

      // Phase 7: Create prompts
      dataInit.createPrompts();

      // Phase 8: Create API keys
      dataInit.createApiKeys();

      // Phase 9: Create tests for all groups
      dataInit.createTestsForAllGroups();

      // Phase 10: Create attempts and submissions
      dataInit.createAttemptsAndSubmissions();

      log.info("Data initialization completed successfully");

    } catch (Exception e) {
      log.error("Error during data initialization: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to initialize data", e);
    }
  }
}
