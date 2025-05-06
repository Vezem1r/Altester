package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.model.Prompt;
import com.altester.ai_grading_service.repository.PromptRepository;
import com.altester.ai_grading_service.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.CacheException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;
    private final CacheManager cacheManager;

    @Value("classpath:prompts/grading_prompt.txt")
    private Resource defaultPromptResource;

    @Override
    public String getPromptById(Long promptId) {
        if (promptId == null) {
            return getDefaultPrompt();
        }

        String cachedPrompt = getPromptFromCache(promptId);
        if (cachedPrompt != null) {
            log.info("Retrieved prompt from cache for ID: {}", promptId);
            return cachedPrompt;
        }

        log.info("Prompt not found in cache, retrieving from database for ID: {}", promptId);

        String promptContent = promptRepository.findById(promptId)
                .map(Prompt::getPrompt)
                .orElseGet(() -> {
                    log.warn("Prompt with ID {} not found, using default prompt", promptId);
                    return getDefaultPrompt();
                });

        cachePromptContent(promptId, promptContent);
        return promptContent;
    }

    @Override
    public String getDefaultPrompt() {
        try (Reader reader = new InputStreamReader(defaultPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error("Failed to load default prompt template", e);
            throw new RuntimeException("Could not load default prompt template", e);
        }
    }

    private String getPromptFromCache(Long promptId) {
        try {
            Cache promptCache = cacheManager.getCache("promptContent");
            if (promptCache != null) {
                String cashedContent = promptCache.get(promptId, String.class);
                if (cashedContent != null) {
                    return cashedContent;
                }
            }
        } catch (CacheException e) {
            log.warn("Error retrieving from promptDetails cache for ID {}: {}", promptId, e.getMessage());
        }
        return null;
    }

    private void cachePromptContent(Long promptId, String promptContent) {
        try {
            Cache promptCache = cacheManager.getCache("promptContent");
            if (promptCache != null) {
                promptCache.put(promptId, promptContent);
                log.debug("Cached prompt content for ID: {}", promptId);
            }
        } catch (Exception e) {
            log.warn("Error caching prompt content for ID {}: {}", promptId, e.getMessage());
        }
    }
}
