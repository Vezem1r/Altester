package com.altester.core.serviceImpl.prompt;

import com.altester.core.dtos.ai_service.PromptDTO;
import com.altester.core.dtos.ai_service.PromptDetailsDTO;
import com.altester.core.model.ApiKey.Prompt;
import org.springframework.stereotype.Component;

@Component
public class PromptMapper {

  public PromptDTO toDTO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    return PromptDTO.builder()
        .id(prompt.getId())
        .title(prompt.getTitle())
        .isPublic(prompt.isPublic())
        .authorUsername(prompt.getAuthor() != null ? prompt.getAuthor().getUsername() : null)
        .created(prompt.getCreated())
        .lastModified(prompt.getLastModified())
        .build();
  }

  public PromptDetailsDTO toDetailsDTO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    return PromptDetailsDTO.builder()
        .id(prompt.getId())
        .title(prompt.getTitle())
        .description(prompt.getDescription())
        .content(prompt.getPrompt())
        .isPublic(prompt.isPublic())
        .authorUsername(prompt.getAuthor() != null ? prompt.getAuthor().getUsername() : null)
        .created(prompt.getCreated())
        .lastModified(prompt.getLastModified())
        .build();
  }
}
