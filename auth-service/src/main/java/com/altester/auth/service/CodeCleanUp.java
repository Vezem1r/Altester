package com.altester.auth.service;

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

    @Scheduled(cron = "0 0 * * * *")
    public void cleanUp() {
        int deletedCount = codeRepository.deleteByVerificationCodeExpiredAtBefore(LocalDateTime.now());
        log.info("Deleted " + deletedCount + "expired verification codes");
    }
}
