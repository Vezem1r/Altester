package com.altester.core.serviceImpl.notification;

import com.altester.core.dtos.notification_service.NotificationRequest;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final RestTemplate restTemplate;

    @Value("${NOTIFICATION_SERVICE_URL}")
    private String notificationServiceUrl;

    @Override
    public void notifyTestAssigned(Test test, Group group) {
        List<String> studentUsernames = group.getStudents().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        if (studentUsernames.isEmpty()) {
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .usernames(studentUsernames)
                .title("New Test")
                .message("You have been assigned a new test '" + test.getTitle() + "' in group '" + group.getName() + "'")
                .type("NEW_TEST_ASSIGNED")
                .actionUrl("/student/tests/" + test.getId())
                .referenceId(test.getId())
                .build();

        sendNotification(request);
    }

    @Override
    public void notifyTestGraded(Attempt attempt) {
        NotificationRequest request = NotificationRequest.builder()
                .usernames(Collections.singletonList(attempt.getStudent().getUsername()))
                .title("Test Graded")
                .message("Your attempt for test '" + attempt.getTest().getTitle() + "' has been graded. Score: " + attempt.getScore())
                .type("TEST_GRADED")
                .actionUrl("/student/tests/" + attempt.getTest().getId() + "/attempts/" + attempt.getId())
                .referenceId(attempt.getId())
                .build();

        sendNotification(request);
    }

    @Override
    public void notifyTeacherFeedback(Attempt attempt) {
        NotificationRequest request = NotificationRequest.builder()
                .usernames(Collections.singletonList(attempt.getStudent().getUsername()))
                .title("Feedback Received")
                .message("You have received feedback for test '" + attempt.getTest().getTitle() + "'")
                .type("TEACHER_FEEDBACK")
                .actionUrl("/student/tests/" + attempt.getTest().getId() + "/attempts/" + attempt.getId() + "/feedback")
                .referenceId(attempt.getId())
                .build();

        sendNotification(request);
    }

    @Override
    public void notifyTestParametersChanged(Test test, Group group) {
        List<String> studentUsernames = group.getStudents().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        if (studentUsernames.isEmpty()) {
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .usernames(studentUsernames)
                .title("Test Parameters Changed")
                .message("Parameters for test '" + test.getTitle() + "' have been updated")
                .type("TEST_PARAMETERS_CHANGED")
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

        NotificationRequest request = NotificationRequest.builder()
                .usernames(Collections.singletonList(group.getTeacher().getUsername()))
                .title("New Student in Group")
                .message(student.getName() + " " + student.getSurname() + " has joined your group '" + group.getName() + "'")
                .type("NEW_STUDENT_JOINED")
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

        List<String> adminUsernames = admins.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        NotificationRequest request = NotificationRequest.builder()
                .usernames(adminUsernames)
                .title("System Warning")
                .message("Test '" + test.getTitle() + "' does not contain any questions")
                .type("SYSTEM_WARNING")
                .actionUrl("/admin/tests/" + test.getId())
                .referenceId(test.getId())
                .build();

        sendNotification(request);
    }

    @Override
    public void notifyUsageStatistics(List<User> admins, int activeTests, int activeUsers, int submissions) {
        if (admins.isEmpty()) {
            return;
        }

        List<String> adminUsernames = admins.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        NotificationRequest request = NotificationRequest.builder()
                .usernames(adminUsernames)
                .title("System Usage Statistics")
                .message("Weekly report: " + activeTests + " active tests, " + activeUsers + " active users, " + submissions + " submitted answers")
                .type("USAGE_STATISTICS")
                .actionUrl("/admin/statistics")
                .build();

        sendNotification(request);
    }

    private void sendNotification(NotificationRequest request) {
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(
                    notificationServiceUrl + "/notifications",
                    request,
                    Object.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error sending notification: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
}