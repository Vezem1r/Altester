package com.altester.core.serviceImpl.apiKey;

import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.ApiKeyRequest;
import com.altester.core.dtos.core_service.apiKey.AvailableKeys;
import com.altester.core.dtos.core_service.apiKey.TestApiKeyAssignmentRequest;
import com.altester.core.exception.*;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.*;
import com.altester.core.service.ApiKeyService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.ApiKeyEncryptionUtil;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

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

    private static final int PREFIX_LENGTH = 8;
    private static final int SUFFIX_LENGTH = 6;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "apiKeys", key = "#principal.name")
    public CacheablePage<ApiKeyDTO> getAll(Principal principal) {
        User currentUser = getUserFromPrincipal(principal);

        List<ApiKey> apiKeys;
        if (RolesEnum.ADMIN.equals(currentUser.getRole())) {
            apiKeys = apiRepository.findAll();
        } else {
            apiKeys = apiRepository.findAllGlobalOrOwnedBy(currentUser);
        }

        List<ApiKeyDTO> apiKeyDTOs = apiKeys.stream()
                .map(key -> ApiKeyDTO.fromEntity(key, currentUser.getId()))
                .toList();

        Pageable pageable = PageRequest.of(0, !apiKeyDTOs.isEmpty() ? apiKeyDTOs.size() : 10);
        Page<ApiKeyDTO> resultPage = new PageImpl<>(apiKeyDTOs, pageable, apiKeyDTOs.size());

        return new CacheablePage<>(resultPage);
    }

    @Override
    @Transactional
    public void createApiKey(ApiKeyRequest request, Principal principal) {
        log.debug("Creating new API key: {}", request.getName());
        User currentUser = getUserFromPrincipal(principal);

        if (request.getIsGlobal() && !RolesEnum.ADMIN.equals(currentUser.getRole())) {
            throw AccessDeniedException.notAdmin();
        }

        try {
            String encryptedKey = encryptionUtil.encrypt(request.getApiKey());
            String keyPrefix = encryptionUtil.extractPrefix(request.getApiKey(), PREFIX_LENGTH);
            String keySuffix = encryptionUtil.extractSuffix(request.getApiKey(), SUFFIX_LENGTH);

            ApiKey apiKey = ApiKey.builder()
                    .name(request.getName())
                    .encryptedKey(encryptedKey)
                    .keyPrefix(keyPrefix)
                    .keySuffix(keySuffix)
                    .aiServiceName(request.getAiServiceName())
                    .isGlobal(request.getIsGlobal())
                    .owner(request.getIsGlobal() ? null : currentUser)
                    .createdAt(LocalDateTime.now())
                    .description(request.getDescription())
                    .isActive(true)
                    .build();

            apiRepository.save(apiKey);
            cacheService.clearApiKeyRelatedCaches();
            log.info("API key created successfully: {} ({})", request.getName(), apiKey.getId());
        } catch (ApiKeyException e) {
            log.error("Error encrypting API key: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteApiKey(Long id, Principal principal) {
        log.debug("Deleting API key with ID: {}", id);
        User currentUser = getUserFromPrincipal(principal);
        ApiKey apiKey = apiRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.apiKey(id));

        validateApiKeyAccessPermission(currentUser, apiKey, "delete");
        apiRepository.deleteById(id);
        cacheService.clearApiKeyRelatedCaches();
        log.info("API key deleted successfully: {} ({})", apiKey.getName(), id);
        return true;
    }

    @Override
    @Transactional
    public void updateApiKey(Long id, ApiKeyRequest request, Principal principal) {
        log.debug("Updating API key with ID: {}", id);
        User currentUser = getUserFromPrincipal(principal);

        ApiKey apiKey = apiRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.apiKey(id));

        validateApiKeyAccessPermission(currentUser, apiKey, "update");

        if (request.getIsGlobal() != apiKey.isGlobal() &&
                !RolesEnum.ADMIN.equals(currentUser.getRole()) ) {
            throw AccessDeniedException.apiKeyAccess("Only admins can change the global status of an API key");
        }

        apiKey.setName(request.getName());
        apiKey.setDescription(request.getDescription());
        apiKey.setAiServiceName(request.getAiServiceName());

        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            try {
                String encryptedKey = encryptionUtil.encrypt(request.getApiKey());
                if (!encryptedKey.equals(apiKey.getEncryptedKey())) {
                    apiKey.setEncryptedKey(encryptedKey);
                    apiKey.setKeyPrefix(encryptionUtil.extractPrefix(request.getApiKey(), PREFIX_LENGTH));
                    apiKey.setKeySuffix(encryptionUtil.extractSuffix(request.getApiKey(), SUFFIX_LENGTH));
                }
            } catch (ApiKeyException e) {
                log.error("Error encrypting API key during update: {}", e.getMessage());
                throw e;
            }
        }
        apiRepository.save(apiKey);
        cacheService.clearApiKeyRelatedCaches();
        log.info("API key updated successfully: {} ({})", apiKey.getName(), id);
    }

    @Override
    @Transactional
    public boolean toggleApiKeyStatus(Long id, Principal principal) {
        log.debug("Toggling API key status with ID: {}", id);
        User currentUser = getUserFromPrincipal(principal);

        ApiKey apiKey = apiRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.apiKey(id));

        validateApiKeyAccessPermission(currentUser, apiKey, "toggle");

        apiKey.setActive(!apiKey.isActive());
        apiRepository.save(apiKey);

        cacheService.clearApiKeyRelatedCaches();

        log.info("API key status toggled for: {} ({}). New status: {}",
                apiKey.getName(), id, apiKey.isActive() ? "active" : "inactive");

        return apiKey.isActive();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "availableApiKeys", key = "#principal.name")
    public List<AvailableKeys> getAvailableApiKeys(Principal principal) {
        User currentUser = getUserFromPrincipal(principal);

        List<ApiKey> apiKeys;
        if (RolesEnum.ADMIN.equals(currentUser.getRole())) {
            apiKeys = apiRepository.findAll();
        } else {
            apiKeys = apiRepository.findAllGlobalOrOwnedBy(currentUser);
        }

        return apiKeys.stream()
                .filter(ApiKey::isActive)
                .map(AvailableKeys::fromApiKey)
                .toList();
    }

    @Override
    @Transactional
    public void assignApiKeyToTestForGroup(TestApiKeyAssignmentRequest request, Principal principal) {
        log.debug("Assigning API key {} to test {}", request.getApiKeyId(), request.getTestId());
        User currentUser = getUserFromPrincipal(principal);

        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> ResourceNotFoundException.test(request.getTestId()));

        ApiKey apiKey = apiRepository.findById(request.getApiKeyId())
                .orElseThrow(() -> ResourceNotFoundException.apiKey(request.getApiKeyId()));

        if (!RolesEnum.ADMIN.equals(currentUser.getRole()) && !apiKey.isGlobal() &&
                (apiKey.getOwner() == null || !apiKey.getOwner().getId().equals(currentUser.getId()))) {
            throw AccessDeniedException.apiKeyAccess("You don't have access to this API key");
        }

        if (request.getGroupId() != null) {
            Group group = getAndValidateGroup(request.getGroupId(), currentUser, test);
            assignApiKeyToTestAndGroup(test, group, apiKey, currentUser);
            log.info("API key {} assigned to test {} for group {} by user {}",
                    apiKey.getId(), test.getId(), group.getId(), currentUser.getUsername());
        } else {
            List<Group> teacherGroups = getTeacherGroupsForTest(currentUser, test);
            if (teacherGroups.isEmpty()) {
                throw ResourceNotFoundException.group("No groups found for this test where you are the teacher");
            }

            for (Group group : teacherGroups) {
                assignApiKeyToTestAndGroup(test, group, apiKey, currentUser);
            }
            log.info("API key {} assigned to test {} for all {} groups by user {}",
                    apiKey.getId(), test.getId(), teacherGroups.size(), currentUser.getUsername());
        }

        cacheService.clearApiKeyRelatedCaches();
        cacheService.clearTestRelatedCaches();
    }

    @Override
    public void unassignApiKeyFromTest(Long testId, Long groupId, Principal principal) {
        log.debug("Unassigning API key from test {}", testId);
        User currentUser = getUserFromPrincipal(principal);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> ResourceNotFoundException.test(testId));

        if (groupId != null) {
            Group group = getAndValidateGroup(groupId, currentUser, test);
            unassignApiKeyFromTestAndGroup(test, group, currentUser);
            log.info("API key unassigned from test {} for group {} by user {}",
                    test.getId(), group.getId(), currentUser.getUsername());
        } else {
            List<Group> teacherGroups = getTeacherGroupsForTest(currentUser, test);

            if (teacherGroups.isEmpty()) {
                throw ResourceNotFoundException.group("No groups found for this test where you are the teacher");
            }

            int unassignedCount = 0;
            for (Group group : teacherGroups) {
                try {
                    unassignApiKeyFromTestAndGroup(test, group, currentUser);
                    unassignedCount++;
                } catch (Exception e) {
                    log.debug("No API key to unassign for test {} and group {}: {}",
                            test.getId(), group.getId(), e.getMessage());
                }
            }

            if (unassignedCount == 0) {
                throw new StateConflictException("assignment", "no_api_key",
                        "This test does not have an API key assigned for any of your groups");
            }
            log.info("API keys unassigned from test {} for {} groups by user {}",
                    test.getId(), unassignedCount, currentUser.getUsername());
        }

        cacheService.clearApiKeyRelatedCaches();
        cacheService.clearTestRelatedCaches();
    }

    private void unassignApiKeyFromTestAndGroup(Test test, Group group, User currentUser) {
        TestGroupAssignment assignment = assignmentRepository
                .findByTestAndGroup(test, group)
                .orElseThrow(() -> new ResourceNotFoundException("assignment", "test and group",
                        test.getId() + " and " + group.getId()));

        if (assignment.getApiKey() == null) {
            throw new StateConflictException("assignment", "no_api_key",
                    "This test does not have an API key assigned for the specified group");
        }

        assignment.setApiKey(null);
        assignmentRepository.save(assignment);
    }

    private List<Group> getTeacherGroupsForTest(User user, Test test) {
        if (RolesEnum.ADMIN.equals(user.getRole())) {
            throw ValidationException.invalidParameter("groupId", "Admins must specify a group ID");
        }

        return groupRepository.findByTeacherAndTestsContaining(user, test);
    }

    private void assignApiKeyToTestAndGroup(Test test, Group group, ApiKey apiKey, User currentUser) {
        TestGroupAssignment assignment = assignmentRepository
                .findByTestAndGroup(test, group)
                .orElse(TestGroupAssignment.builder()
                        .test(test)
                        .group(group)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(currentUser)
                        .build());

        assignment.setApiKey(apiKey);
        assignmentRepository.save(assignment);
    }

    private Group getAndValidateGroup(Long groupId, User user, Test test) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> ResourceNotFoundException.group(groupId));

        if (!group.getTests().contains(test)) {
            throw new StateConflictException("group", "mismatch", "The specified group is not associated with this test");
        }

        if (!RolesEnum.ADMIN.equals(user.getRole()) &&
                (group.getTeacher() == null || !group.getTeacher().getId().equals(user.getId()))) {
            throw AccessDeniedException.groupAccess();
        }

        return group;
    }

    private void validateApiKeyAccessPermission(User currentUser, ApiKey apiKey, String operation) {
        if (!RolesEnum.ADMIN.equals(currentUser.getRole()) &&
                (apiKey.isGlobal() ||
                        apiKey.getOwner() == null ||
                        !apiKey.getOwner().getId().equals(currentUser.getId()))) {
            throw AccessDeniedException.apiKeyAccess("You cannot " + operation + " this API key");
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
    }
}
