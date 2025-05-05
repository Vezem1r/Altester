package com.altester.core.model.subject.enums;

public enum NotificationType {
    // Student
    NEW_TEST_ASSIGNED,
    TEST_GRADED,
    TEACHER_FEEDBACK,
    TEST_PARAMETERS_CHANGED,

    // Teacher
    NEW_STUDENT_JOINED,
    ADMIN_TEST_CHANGED,
    REGRADE_REQUESTED,

    // Admin
    SYSTEM_WARNING,
    USAGE_STATISTICS
}