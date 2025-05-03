package com.altester.core.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(
                cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                });
        log.debug("All application caches have been cleared.");
    }

    public void clearTestRelatedCaches() {
        clearCaches(
                "tests",
                "test",
                "testSummary",
                "testsBySubject",
                "testsByGroup"
        );
        log.debug("All test-related caches have been cleared");
    }

    public void clearStudentRelatedCaches() {
        clearCaches(
                "students",
                "studentDashboard",
                "academicHistory",
                "availablePeriods",
                "studentTestAttempts",
                "attemptReview"
        );
        log.debug("All student-related caches have been cleared");
    }

    public void clearGroupRelatedCaches() {
        clearCaches(
                "groups",
                "group",
                "groupStudents",
                "groupTeachers",
                "groupStudentsWithCategories",
                "groupStudentsNotInGroup"
        );
        log.debug("All group-related caches have been cleared");
    }

    public void clearSubjectRelatedCaches() {
        clearCaches("subjects");
        clearGroupRelatedCaches();
        clearTestRelatedCaches();
        log.debug("All subject-related caches have been cleared");
    }

    public void clearTeacherRelatedCaches() {
        clearCaches(
                "teachers",
                "teacherPage",
                "teacherStudents",
                "teacherGroups",
                "testAttemptsForTeacher",
                "studentAttemptsForTeacher"
        );
        log.debug("All teacher-related caches have been cleared");
    }

    public void clearAdminRelatedCaches() {
        clearCaches(
                "adminStats",
                "testAttemptsForAdmin",
                "studentAttemptsForAdmin"
        );
        log.debug("All admin-related caches have been cleared");
    }

    public void clearAttemptRelatedCaches() {
        clearCaches(
                "attemptReview",
                "testAttemptsForTeacher",
                "testAttemptsForAdmin",
                "studentAttemptsForTeacher",
                "studentAttemptsForAdmin",
                "studentTestAttempts"
        );
        log.debug("All attempt-related caches have been cleared");
    }

    public void clearQuestionRelatedCaches() {
        clearCaches("questions", "question");
        clearTestRelatedCaches();
        log.debug("All question-related caches have been cleared");
    }

    public void clearApiKeyRelatedCaches() {
        clearCaches(
                "apiKeys",
                "availableApiKeys",
                "testApiKeys"
        );
        log.debug("All API key-related caches have been cleared");
    }

    public void clearPromptRelatedCaches() {
        clearCaches(
                "prompts",
                "myPrompts",
                "publicPrompts",
                "promptDetails"
        );
        log.debug("All prompt-related caches have been cleared");
    }

    public void clearCaches(String... cacheNames) {
        Arrays.stream(cacheNames).forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Cache '{}' cleared", cacheName);
            } else {
                log.warn("Cache '{}' not found", cacheName);
            }
        });
    }
}
