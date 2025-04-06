package com.altester.core.service;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;

import java.util.List;

public interface NotificationDispatchService {
    void notifyTestAssigned(Test test, Group group);

    void notifyTestGraded(Attempt attempt);

    void notifyTeacherFeedback(Attempt attempt);

    void notifyTestParametersChanged(Test test, Group group);

    void notifyNewStudentJoined(User student, Group group);

    void notifyTestWithoutQuestions(Test test, List<User> admins);

    void notifyUsageStatistics(List<User> admins, int activeTests, int activeUsers, int submissions);
}