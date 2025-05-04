package com.altester.ai_grading_service.service.impl;

import com.altester.ai_grading_service.model.Prompt;
import com.altester.ai_grading_service.repository.PromptRepository;
import com.altester.ai_grading_service.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    @Value("classpath:prompts/grading_prompt.txt")
    private Resource defaultPromptResource;

    @Override
    @Cacheable(value = "prompts", key = "#promptId")
    public String getPromptById(Long promptId) {
        if (promptId == null) {
            return getDefaultPrompt();
        }

        return promptRepository.findById(promptId)
                .map(Prompt::getPrompt)
                .orElseGet(() -> {
                    log.warn("Prompt with ID {} not found, using default prompt", promptId);
                    return getDefaultPrompt();
                });
    }

    @Override
    @Cacheable(value = "prompts", key = "'default'")
    public String getDefaultPrompt() {
        try (Reader reader = new InputStreamReader(defaultPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error("Failed to load default prompt template", e);
            throw new RuntimeException("Could not load default prompt template", e);
        }
    }
}
