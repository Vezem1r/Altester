package com.altester.core.service;

import com.altester.core.dtos.ai_service.PromptDTO;
import com.altester.core.dtos.ai_service.PromptDetailsDTO;
import com.altester.core.dtos.ai_service.PromptRequest;
import com.altester.core.util.CacheablePage;
import java.security.Principal;
import org.springframework.data.domain.Pageable;

public interface PromptService {

  PromptDetailsDTO createPrompt(PromptRequest request, Principal principal);

  PromptDetailsDTO updatePrompt(Long id, PromptRequest request, Principal principal);

  void deletePrompt(Long id, Principal principal);

  PromptDetailsDTO getPromptDetails(Long id, Principal principal);

  CacheablePage<PromptDTO> getAllPrompts(Pageable pageable, Principal principal);

  CacheablePage<PromptDTO> getMyPrompts(Pageable pageable, Principal principal);

  CacheablePage<PromptDTO> getPublicPrompts(Pageable pageable);
}
