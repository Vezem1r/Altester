package com.altester.chat_service.service;

import com.altester.chat_service.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCleanupService {

  private final ChatMessageRepository chatMessageRepository;

  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional
  public void cleanupOldMessages() {
    LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
    log.info("Starting cleanup of old messages before {}", fourWeeksAgo);
    int deletedCount = chatMessageRepository.deleteOldReadMessages(fourWeeksAgo);
    log.info("Deleted {} old messages", deletedCount);
  }
}
