package com.altester.core.service;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.NotificationType;
import java.util.List;
import org.springframework.http.HttpStatus;

public interface NotificationDispatchService {

  /**
   * Notifies all students in a group when a test has been assigned to them. Sends a notification
   * with test title, group name, and direct link to the test.
   *
   * @param test The test that has been assigned
   * @param group The group to which the test was assigned
   */
  void notifyTestAssigned(Test test, Group group);

  /**
   * Notifies a student when their test attempt has been graded. Includes score information and a
   * link to view the graded attempt.
   *
   * @param attempt The attempt that has been graded
   */
  void notifyTestGraded(Attempt attempt);

  /**
   * Notifies a student when their test attempt has been graded by AI. Includes score information
   * and a link to view the graded attempt.
   *
   * @param attempt The attempt that has been graded
   */
  void notifyTestGradedByAi(Attempt attempt);

  /**
   * Notifies a student when they have received feedback from a teacher on their attempt. Includes a
   * link to view the detailed feedback.
   *
   * @param attempt The attempt that has received feedback
   */
  void notifyTeacherFeedback(Attempt attempt);

  /**
   * Notifies all students in a group when the parameters of a test have been changed. This includes
   * changes to title, duration, start/end time, etc.
   *
   * @param test The test with updated parameters
   * @param group The group associated with the test
   */
  void notifyTestParametersChanged(Test test, Group group);

  /**
   * Notifies a teacher when a new student joins one of their groups. Includes student name and a
   * link to the group's student list.
   *
   * @param student The student who joined the group
   * @param group The group the student joined
   */
  void notifyNewStudentJoined(User student, Group group);

  /**
   * Notifies system administrators when a test is created without any questions. Includes a link to
   * the test editor to add questions.
   *
   * @param test The test without questions
   * @param admins List of administrator users to notify
   */
  void notifyTestWithoutQuestions(Test test, List<User> admins);

  /**
   * Sends weekly system usage statistics to administrators. Includes metrics on active tests,
   * active users, and submissions.
   *
   * @param admins List of administrator users to notify
   * @param activeTests Number of active tests in the system
   * @param activeUsers Number of active users in the system
   * @param submissions Number of submissions in the period
   */
  void notifyUsageStatistics(List<User> admins, int activeTests, int activeUsers, int submissions);

  /**
   * Notifies teacher when a student requests re-grading of submissions
   *
   * @param student The student requesting re-grading
   * @param teacher The teacher who will review the request
   * @param test The test containing the submissions
   * @param questionCount Number of questions requested for re-grading
   */
  void notifyRegradeRequested(User student, User teacher, Test test, int questionCount);

  /**
   * Notifies the API key owner or system administrators about API key errors. If the API key has no
   * owner, the notification is sent to admin users.
   *
   * @param apiKey The API key that experienced an error
   * @param errorMessage The error message describing what went wrong
   * @param status The HTTP status code associated with the error
   * @param errorType The type of notification error
   * @param title The title of the notification to send
   * @param message The message body of the notification to send
   */
  void notifyApiKeyError(
      ApiKey apiKey,
      String errorMessage,
      HttpStatus status,
      NotificationType errorType,
      String title,
      String message);
}
