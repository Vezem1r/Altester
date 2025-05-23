package com.altester.core.serviceImpl.apiKey;

import com.altester.core.config.AiModelConfiguration;
import com.altester.core.dtos.core_service.apiKey.*;
import com.altester.core.exception.*;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.*;
import com.altester.core.service.ApiKeyService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.ApiKeyEncryptionUtil;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

  private final ApiKeyRepository apiRepository;
  private final UserRepository userRepository;
  private final ApiKeyEncryptionUtil encryptionUtil;
  private final CacheService cacheService;
  private final TestRepository testRepository;
  private final GroupRepository groupRepository;
  private final TestGroupAssignmentRepository assignmentRepository;
  private final AiModelConfiguration modelConfiguration;

  private final ApiKeyAccessValidator accessValidator;

  private static final int PREFIX_LENGTH = 8;
  private static final int SUFFIX_LENGTH = 6;
  private final PromptRepository promptRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "apiKeys", key = "#principal.name")
  public CacheablePage<ApiKeyDTO> getAll(Principal principal) {
    User currentUser = accessValidator.getUserFromPrincipal(principal);

    List<ApiKey> apiKeys;
    if (RolesEnum.ADMIN.equals(currentUser.getRole())) {
      apiKeys = apiRepository.findAll();
    } else {
      apiKeys = apiRepository.findAllGlobalOrOwnedBy(currentUser);
    }

    List<ApiKeyDTO> apiKeyDTOs =
        apiKeys.stream().map(key -> ApiKeyDTO.fromEntity(key, currentUser.getId())).toList();

    Pageable pageable = PageRequest.of(0, !apiKeyDTOs.isEmpty() ? apiKeyDTOs.size() : 10);
    Page<ApiKeyDTO> resultPage = new PageImpl<>(apiKeyDTOs, pageable, apiKeyDTOs.size());

    return new CacheablePage<>(resultPage);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "availableApiKeys", key = "#principal.name")
  public List<AvailableKeys> getAvailableApiKeys(Principal principal) {
    User currentUser = accessValidator.getUserFromPrincipal(principal);

    List<ApiKey> apiKeys;
    if (RolesEnum.ADMIN.equals(currentUser.getRole())) {
      apiKeys = apiRepository.findAll();
    } else {
      apiKeys = apiRepository.findAllGlobalOrOwnedBy(currentUser);
    }

    return apiKeys.stream().filter(ApiKey::isActive).map(AvailableKeys::fromApiKey).toList();
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "testApiKeys", key = "#principal.name + ':test:' + #testId")
  public TestApiKeysDTO getTestApiKeys(Long testId, Principal principal) {
    log.debug("Getting API keys for test ID: {}", testId);

    User currentUser = accessValidator.getUserFromPrincipal(principal);
    Test test =
        testRepository.findById(testId).orElseThrow(() -> ResourceNotFoundException.test(testId));

    List<ApiKeyAssignmentDTO> assignments = new ArrayList<>();

    for (TestGroupAssignment assignment : test.getTestGroupAssignments()) {
      ApiKey apiKey = assignment.getApiKey();
      Group group = assignment.getGroup();
      User teacher = group.getTeacher();

      boolean shouldInclude =
          apiKey != null
              && (currentUser.getRole() != RolesEnum.TEACHER
                  || (teacher != null && teacher.getId().equals(currentUser.getId())));

      if (shouldInclude) {
        String maskedKey = apiKey.getKeyPrefix() + "*".repeat(8) + apiKey.getKeySuffix();

        GroupTeacherDTO groupTeacherDTO =
            GroupTeacherDTO.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .teacherUsername(teacher != null ? teacher.getUsername() : null)
                .build();

        ApiKeyAssignmentDTO assignmentDTO =
            ApiKeyAssignmentDTO.builder()
                .apiKeyId(apiKey.getId())
                .apiKeyName(apiKey.getName())
                .maskedKey(maskedKey)
                .aiServiceName(apiKey.getAiServiceName())
                .model(apiKey.getModel())
                .promptName(assignment.getPrompt().getTitle())
                .group(groupTeacherDTO)
                .aiEvaluationEnabled(assignment.isAiEvaluation())
                .build();

        assignments.add(assignmentDTO);
      }
    }

    if (assignments.isEmpty() && currentUser.getRole() == RolesEnum.TEACHER) {
      List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
      boolean hasAccessToTest =
          teacherGroups.stream().anyMatch(group -> group.getTests().contains(test));

      if (!hasAccessToTest) {
        log.warn(
            "Teacher {} attempted to access API keys for test {} but has no associated groups",
            currentUser.getUsername(),
            testId);
        throw AccessDeniedException.testAccess();
      }
    }

    log.info(
        "Retrieved {} API key assignments for test ID {} for user {}",
        assignments.size(),
        testId,
        currentUser.getUsername());

    return TestApiKeysDTO.builder().assignments(assignments).build();
  }
}
