package com.altester.core.serviceImpl.question;

import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.TestRepository;
import com.altester.core.service.NotificationDispatchService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.serviceImpl.test.TestDTOMapper;
import com.altester.core.serviceImpl.test.TestRequirementsValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestStatusService {

  private final TestRequirementsValidator testRequirementsValidator;
  private final TestRepository testRepository;
  private final TestDTOMapper testDTOMapper;
  private final NotificationDispatchService notificationService;
  private final CacheService cacheService;

  public void updateTestOpenStatus(Test test) {

    if (!testRequirementsValidator.requirementsMet(test)) {
      test.setOpen(false);
      testRepository.save(test);

      String message = testRequirementsValidator.getMissingRequirements(test);
      log.info(
          "Test with ID {} was closed because it no longer meets difficulty requirements: {}",
          test.getId(),
          message);

      List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
      for (Group group : testGroups) {
        notificationService.notifyTestParametersChanged(test, group);
      }
    }
    cacheService.clearTestRelatedCaches();
  }
}
