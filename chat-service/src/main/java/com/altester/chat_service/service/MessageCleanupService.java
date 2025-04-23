package com.altester.chat_service.service;

import com.altester.chat_service.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
        log.info("Starting cleanup of old messages before {}", twoWeeksAgo);
        int deletedCount = chatMessageRepository.deleteOldReadMessages(twoWeeksAgo);
        log.info("Deleted {} old messages", deletedCount);
    }
}