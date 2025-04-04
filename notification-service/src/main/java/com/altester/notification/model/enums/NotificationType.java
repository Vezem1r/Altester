package com.altester.notification.model.enums;

public enum NotificationType {
    // Student
    NEW_TEST_ASSIGNED,
    TEST_GRADED,
    TEACHER_FEEDBACK,
    TEST_PARAMETERS_CHANGED,

    // Teacher
    GROUP_PERFORMANCE,
    NEW_STUDENT_JOINED,
    ADMIN_TEST_CHANGED,

    // Admin
    SYSTEM_WARNING,
    USAGE_STATISTICS
}