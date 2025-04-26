package com.altester.core.serviceImpl.apiKey;

import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.ApiKeyRequest;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ApiKeyException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.ApiKey;
import com.altester.core.repository.ApiKeyRepository;
import com.altester.core.repository.UserRepository;
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

        if (!RolesEnum.ADMIN.equals(currentUser.getRole())
                && (apiKey.isGlobal() ||
                (apiKey.getOwner() != null && !apiKey.getOwner().getId().equals(currentUser.getId())))) {
            throw AccessDeniedException.apiKeyAccess("You cannot delete this API key");
        }
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

        if (!RolesEnum.ADMIN.equals(currentUser.getRole()) &&
                (apiKey.isGlobal() ||
                        apiKey.getOwner() == null ||
                        !apiKey.getOwner().getId().equals(currentUser.getId()))) {
            throw AccessDeniedException.apiKeyAccess("You cannot update this API key");
        }

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
    @Cacheable(value = "availableApiKeys", key = "#principal.name")
    public List<ApiKeyDTO> getAvailableApiKeys(Principal principal) {
        User currentUser = getUserFromPrincipal(principal);

        List<ApiKey> apiKeys;
        if (RolesEnum.ADMIN.equals(currentUser.getRole())) {
            apiKeys = apiRepository.findAll();
        } else {
            apiKeys = apiRepository.findAllGlobalOrOwnedBy(currentUser);
        }

        return apiKeys.stream()
                .filter(ApiKey::isActive)
                .map(key -> ApiKeyDTO.fromEntity(key, currentUser.getId()))
                .toList();
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
    }
}
