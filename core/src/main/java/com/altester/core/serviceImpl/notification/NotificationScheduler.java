package com.altester.core.serviceImpl.notification;

import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.AttemptRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationDispatchService notificationService;
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final AttemptRepository attemptRepository;

    @Scheduled(cron = "0 0 8 ? * MON")
    public void sendWeeklyStatistics() {
        int activeTests = testRepository.countOpenTests();
        int activeUsers = userRepository.countByLastLoginAfter(
                LocalDateTime.now().minusDays(7));
        int submissions = attemptRepository.countByEndTimeAfter(
                LocalDateTime.now().minusDays(7));

        List<User> admins = userRepository.findAllByRole(RolesEnum.ADMIN);

        if (admins.isEmpty()) {
            log.info("No admins found to send weekly statistics");
        }
        notificationService.notifyUsageStatistics(
                admins, activeTests, activeUsers, submissions);

        log.info("Send weekly statistics completed");
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void checkTestsWithoutQuestions() {
        try {
            List<Test> testsWithoutQuestions = testRepository.findAll().stream()
                    .filter(test -> test.isOpen() &&
                            (test.getQuestions() == null || test.getQuestions().isEmpty()))
                    .toList();

            if (testsWithoutQuestions.isEmpty()) {
                log.info("No open tests without questions found");
            }

            List<User> admins = userRepository.findAllByRole(RolesEnum.ADMIN);

            if (admins.isEmpty()) {
                log.warn("No administrators found to send test warnings");
            }

            for (Test test : testsWithoutQuestions) {
                notificationService.notifyTestWithoutQuestions(test, admins);
            }

            log.info("Sent notifications about {} tests without questions to {} administrators",
                    testsWithoutQuestions.size(), admins.size());
        } catch (Exception e) {
            log.error("Error checking tests without questions", e);
        }
    }
}