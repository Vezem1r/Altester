package com.altester.auth.service.serviceImpl;

import com.altester.auth.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodeCleanUp {

    private final CodeRepository codeRepository;

    /**
     * Scheduled method to delete expired verification codes.
     * Runs hourly (at the beginning of each hour) to remove all codes
     * that have passed their expiration time from the database.
     * The method logs the number of deleted codes for monitoring.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanUp() {
        int deletedCount = codeRepository.deleteByExpirationBefore(LocalDateTime.now());
        log.info("Deleted {} expired verification codes", deletedCount);
    }
}
