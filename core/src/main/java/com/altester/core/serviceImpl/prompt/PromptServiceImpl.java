package com.altester.core.serviceImpl.prompt;

import com.altester.core.dtos.ai_service.PromptDTO;
import com.altester.core.dtos.ai_service.PromptDetailsDTO;
import com.altester.core.dtos.ai_service.PromptRequest;
import com.altester.core.exception.PromptException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.ApiKey.Prompt;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.repository.PromptRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.PromptService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.CacheablePage;
import com.altester.core.util.PromptValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final PromptMapper promptMapper;
    private final PromptValidator promptValidator;
    private final CacheService cacheService;

    @Value("${prompt.teacher.max-count}")
    private int maxPromptsPerTeacher;

    @Override
    @Transactional
    public PromptDetailsDTO createPrompt(PromptRequest request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        if (user.getRole() == RolesEnum.TEACHER) {
            long currentPromptCount = promptRepository.countByAuthor(user);
            if (currentPromptCount >= maxPromptsPerTeacher) {
                throw PromptException.promptLimitExceeded(maxPromptsPerTeacher);
            }
        }

        String sanitizedContent = promptValidator.sanitizePrompt(request.getContent());
        promptValidator.validatePrompt(sanitizedContent);

        Prompt prompt = Prompt.builder()
                .title(request.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : null)
                .prompt(sanitizedContent)
                .author(user)
                .isPublic(request.isPublic())
                .created(LocalDateTime.now())
                .build();

        prompt = promptRepository.save(prompt);
        log.info("Prompt created with id: {} by user: {}", prompt.getId(), principal.getName());
        cacheService.clearPromptRelatedCaches();
        return promptMapper.toDetailsDTO(prompt);
    }

    @Override
    @Transactional
    public PromptDetailsDTO updatePrompt(Long id, PromptRequest request, Principal principal) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> PromptException.promptNotFound(id));

        if (Long.valueOf(1).equals(prompt.getId())) {
            throw PromptException.unauthorizedPromptModification();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        if (!isUserAuthorizedToModify(prompt, user)) {
            throw PromptException.unauthorizedPromptModification();
        }

        String sanitizedContent = promptValidator.sanitizePrompt(request.getContent());
        promptValidator.validatePrompt(sanitizedContent);

        prompt.setTitle(request.getTitle().trim());
        prompt.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        prompt.setPrompt(sanitizedContent);
        prompt.setPublic(request.isPublic());
        prompt.setLastModified(LocalDateTime.now());

        prompt = promptRepository.save(prompt);
        log.info("Prompt updated with id: {} by user: {}", prompt.getId(), principal.getName());
        cacheService.clearPromptRelatedCaches();
        return promptMapper.toDetailsDTO(prompt);
    }

    @Override
    @Transactional
    public void deletePrompt(Long id, Principal principal) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> PromptException.promptNotFound(id));

        if (Long.valueOf(1).equals(prompt.getId())) {
            throw PromptException.unauthorizedPromptModification();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        if (!isUserAuthorizedToModify(prompt, user)) {
            throw PromptException.unauthorizedPromptModification();
        }

        promptRepository.delete(prompt);
        cacheService.clearPromptRelatedCaches();
        log.info("Prompt deleted with id: {} by user: {}", id, principal.getName());
    }

    @Override
    @Cacheable(value = "promptDetails", key = "#id")
    public PromptDetailsDTO getPromptDetails(Long id, Principal principal) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> PromptException.promptNotFound(id));

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        if (!isUserAuthorizedToView(prompt, user)) {
            throw PromptException.unauthorizedPromptAccess();
        }

        return promptMapper.toDetailsDTO(prompt);
    }

    @Override
    @Cacheable(value = "prompts", key = "#pageable")
    public CacheablePage<PromptDTO> getAllPrompts(Pageable pageable, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        if (user.getRole() != RolesEnum.ADMIN) {
            throw PromptException.unauthorizedPromptAccess();
        }

        Page<PromptDTO> response = promptRepository.findAll(pageable).map(promptMapper::toDTO);
        return new CacheablePage<>(response);
    }

    @Override
    @Cacheable(value = "myPrompts", key = "#principal.name + '_' + #pageable")
    public CacheablePage<PromptDTO> getMyPrompts(Pageable pageable, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getName()));

        Page<PromptDTO> page = promptRepository.findByAuthor(user, pageable).map(promptMapper::toDTO);
        return new CacheablePage<>(page);
    }

    @Override
    @Cacheable(value = "publicPrompts", key = "#pageable")
    public CacheablePage<PromptDTO> getPublicPrompts(Pageable pageable) {
        Page<PromptDTO> page = promptRepository.findAllIsPublicTrue(pageable).map(promptMapper::toDTO);
        return new CacheablePage<>(page);
    }

    private boolean isUserAuthorizedToView(Prompt prompt, User user) {
        if (user.getRole() == RolesEnum.ADMIN) {
            return true;
        }
        if (prompt.getAuthor().getId().equals(user.getId())) {
            return true;
        }
        return prompt.isPublic();
    }

    private boolean isUserAuthorizedToModify(Prompt prompt, User user) {
        if (user.getRole() == RolesEnum.ADMIN) {
            return true;
        }
        return prompt.getAuthor().getId().equals(user.getId());
    }
}
