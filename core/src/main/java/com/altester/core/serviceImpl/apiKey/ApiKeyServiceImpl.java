package com.altester.core.serviceImpl.apiKey;

import com.altester.core.dtos.core_service.apiKey.ApiKeyDTO;
import com.altester.core.dtos.core_service.apiKey.ApiKeyRequest;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.ApiKey;
import com.altester.core.repository.ApiKeyRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.ApiKeyService;
import com.altester.core.util.ApiKeyEncryptionUtil;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final int PREFIX_LENGTH = 4;
    private static final int SUFFIX_LENGTH = 4;

    @Override
    @Transactional(readOnly = true)
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
        User currentUser = getUserFromPrincipal(principal);

        if (request.getIsGlobal() && !RolesEnum.ADMIN.equals(currentUser.getRole())) {
            throw AccessDeniedException.notAdmin();
        }

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
    }

    @Override
    @Transactional
    public boolean deleteApiKey(Long id, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        ApiKey apiKey = apiRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.apiKey(id));

        if (!RolesEnum.ADMIN.equals(currentUser.getRole())
        && (apiKey.isGlobal() ||
                (apiKey.getOwner() != null && !apiKey.getOwner().getId().equals(currentUser.getId())))) {
            throw AccessDeniedException.apiKeyAccess();
        }
        apiRepository.deleteById(id);
        return true;
    }

    @Override
    public ApiKeyDTO updateApiKey(Long id, ApiKeyRequest request, Principal principal) {
        return null;
    }

    @Override
    public List<ApiKeyDTO> getAvailableApiKeys(Principal principal) {
        return List.of();
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));
    }
}
