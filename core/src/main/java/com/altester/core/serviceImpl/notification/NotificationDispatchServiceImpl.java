package com.altester.core.serviceImpl.notification;

import com.altester.core.config.AppConfig;
import com.altester.core.dtos.notification_service.NotificationRequest;
import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.NotificationType;
import com.altester.core.repository.UserRepository;
import com.altester.core.service.NotificationDispatchService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

  private static final String REVIEW_ACTION_URL = "/student/attempt-review/";

  private final RestTemplate restTemplate;
  private final AppConfig appConfig;
  private final UserRepository userRepository;

  @Override
  public void notifyTestAssigned(Test test, Group group) {
    List<String> studentUsernames = group.getStudents().stream().map(User::getUsername).toList();

    if (studentUsernames.isEmpty()) {
      return;
    }

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(studentUsernames)
            .title("New Test")
            .message(
                "You have been assigned a new test '"
                    + test.getTitle()
                    + "' in group '"
                    + group.getName()
                    + "'")
            .type(NotificationType.NEW_TEST_ASSIGNED.toString())
            .actionUrl("/student/tests/" + test.getId())
            .referenceId(test.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyTestGraded(Attempt attempt) {
    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(attempt.getStudent().getUsername()))
            .title("Test Graded")
            .message(
                "Your attempt for test '"
                    + attempt.getTest().getTitle()
                    + "' has been graded. Score: "
                    + attempt.getScore())
            .type(NotificationType.TEST_GRADED.toString())
            .actionUrl(REVIEW_ACTION_URL + attempt.getId())
            .referenceId(attempt.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyTestGradedByAi(Attempt attempt) {
    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(attempt.getStudent().getUsername()))
            .title("Test Graded")
            .message(
                "Your attempt for test '"
                    + attempt.getTest().getTitle()
                    + "' has been graded by AI. Score: "
                    + attempt.getAiScore())
            .type(NotificationType.TEST_GRADED.toString())
            .actionUrl(REVIEW_ACTION_URL + attempt.getId())
            .referenceId(attempt.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyTeacherFeedback(Attempt attempt) {
    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(attempt.getStudent().getUsername()))
            .title("Feedback Received")
            .message("You have received feedback for test '" + attempt.getTest().getTitle() + "'")
            .type(NotificationType.TEACHER_FEEDBACK.toString())
            .actionUrl(REVIEW_ACTION_URL + attempt.getId())
            .referenceId(attempt.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyTestParametersChanged(Test test, Group group) {
    List<String> studentUsernames = group.getStudents().stream().map(User::getUsername).toList();

    if (studentUsernames.isEmpty()) {
      return;
    }

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(studentUsernames)
            .title("Test Parameters Changed")
            .message("Parameters for test '" + test.getTitle() + "' have been updated")
            .type(NotificationType.TEST_PARAMETERS_CHANGED.toString())
            .actionUrl("/student/tests/" + test.getId())
            .referenceId(test.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyNewStudentJoined(User student, Group group) {
    if (group.getTeacher() == null) {
      return;
    }

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(group.getTeacher().getUsername()))
            .title("New Student in Group")
            .message(
                student.getName()
                    + " "
                    + student.getSurname()
                    + " has joined your group '"
                    + group.getName()
                    + "'")
            .type(NotificationType.NEW_STUDENT_JOINED.toString())
            .actionUrl("/teacher/groups/" + group.getId() + "/students")
            .referenceId(group.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyTestWithoutQuestions(Test test, List<User> admins) {
    if (admins.isEmpty()) {
      return;
    }

    List<String> adminUsernames = admins.stream().map(User::getUsername).toList();

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(adminUsernames)
            .title("System Warning")
            .message("Test '" + test.getTitle() + "' does not contain any questions")
            .type(NotificationType.SYSTEM_WARNING.toString())
            .actionUrl("/admin/tests/" + test.getId())
            .referenceId(test.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyUsageStatistics(
      List<User> admins, int activeTests, int activeUsers, int submissions) {
    if (admins.isEmpty()) {
      return;
    }

    List<String> adminUsernames = admins.stream().map(User::getUsername).toList();

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(adminUsernames)
            .title("System Usage Statistics")
            .message(
                "Weekly report: "
                    + activeTests
                    + " active tests, "
                    + activeUsers
                    + " active users, "
                    + submissions
                    + " submitted answers")
            .type(NotificationType.USAGE_STATISTICS.toString())
            .actionUrl("/admin/statistics")
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyRegradeRequested(User student, User teacher, Test test, int questionCount) {
    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(teacher.getUsername()))
            .title("Regrade Request")
            .message(
                student.getUsername()
                    + " has requested re-grading for "
                    + questionCount
                    + " question(s) in test '"
                    + test.getTitle()
                    + "'")
            .type(NotificationType.REGRADE_REQUESTED.toString())
            .actionUrl("/teacher/tests/" + test.getId() + "/attempts")
            .referenceId(test.getId())
            .build();

    sendNotification(request);
  }

  @Override
  public void notifyApiKeyError(
      ApiKey apiKey,
      String errorMessage,
      HttpStatus status,
      NotificationType errorType,
      String title,
      String message) {
    User owner = apiKey.getOwner();
    if (owner == null) {
      List<User> admins = userRepository.findAllByRole(RolesEnum.ADMIN);
      if (admins.isEmpty()) {
        log.warn(
            "No admin users found. Cannot notify about API key error for key {}", apiKey.getId());
        return;
      }
      owner = admins.getFirst();
    }

    if (owner == null || owner.getUsername() == null || owner.getUsername().isEmpty()) {
      log.warn("Cannot send notification for API key {} - no valid owner username", apiKey.getId());
      return;
    }

    String ownerUsername = owner.getUsername();

    NotificationRequest request =
        NotificationRequest.builder()
            .usernames(Collections.singletonList(ownerUsername))
            .title(title)
            .message(message)
            .type(errorType.toString())
            .actionUrl("/api-keys")
            .referenceId(apiKey.getId())
            .build();

    sendNotification(request);
  }

  private void sendNotification(NotificationRequest request) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("x-api-key", appConfig.getApiKey());

      HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<?> response =
          restTemplate.exchange(
              appConfig.getNotificationUrl(), HttpMethod.POST, entity, Object.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("Error sending notification: {}", response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Error sending notification", e);
    }
  }
}
