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
  USAGE_STATISTICS,

  // API Key errors
  API_KEY_INVALID, // 401 - Invalid authentication
  API_KEY_ERROR, // General API errors
  API_KEY_RATE_LIMITED, // 429 - Rate limit exceeded
  API_KEY_QUOTA_EXCEEDED, // 429/402 - Out of credits/balance
  API_KEY_PERMISSION_DENIED, // 403 - Access forbidden
  API_KEY_BAD_REQUEST, // 400/422 - Invalid request format
  API_KEY_SERVER_ERROR, // 500+ - Service provider issues
  API_KEY_NOT_FOUND // 404 - Resource not found
}
