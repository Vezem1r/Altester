package com.altester.core.serviceImpl.attemptRetrieval;

import com.altester.core.dtos.core_service.retrieval.*;
import com.altester.core.dtos.core_service.review.AttemptReviewSubmissionDTO;
import com.altester.core.dtos.core_service.student.AttemptReviewDTO;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.auth.User;
import com.altester.core.model.subject.*;
import com.altester.core.repository.*;
import com.altester.core.service.AttemptRetrievalService;
import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptRetrievalServiceImpl implements AttemptRetrievalService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final AttemptRepository attemptRepository;
  private final TestRepository testRepository;
  private final AttemptAccessValidator accessValidator;
  private final AttemptDataProcessor dataProcessor;
  private final AttemptReviewService reviewService;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "testAttemptsForTeacher",
      key =
          "#principal.name + ':testId:' + #testId + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
  public List<TestAttemptsForGroupDTO> getTestAttemptsForTeacher(
      Principal principal, Long testId, String searchQuery) {

    log.info(
        "Teacher {} requesting attempts for test {}, search query: {}",
        principal.getName(),
        testId,
        searchQuery);

    User teacher = accessValidator.getUserFromPrincipal(principal);
    accessValidator.verifyTeacherRole(teacher);

    testRepository
        .findById(testId)
        .orElseThrow(() -> new ResourceNotFoundException("Test", testId.toString(), null));

    List<Group> teacherGroups =
        groupRepository.findByTeacher(teacher).stream()
            .filter(group -> group.getTests().stream().anyMatch(t -> t.getId() == testId))
            .toList();

    List<Attempt> allAttempts = attemptRepository.findAll();
    return dataProcessor.processGroupsForTestAttempts(
        teacherGroups, testId, allAttempts, searchQuery);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "testAttemptsForAdmin",
      key =
          "#principal.name + ':testId:' + #testId + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
  public List<TestAttemptsForGroupDTO> getTestAttemptsForAdmin(
      Principal principal, Long testId, String searchQuery) {

    log.info(
        "Admin {} requesting attempts for test {}, search query: {}",
        principal.getName(),
        testId,
        searchQuery);

    User admin = accessValidator.getUserFromPrincipal(principal);
    accessValidator.verifyAdminRole(admin);

    testRepository
        .findById(testId)
        .orElseThrow(() -> new ResourceNotFoundException("Test", testId.toString(), null));

    List<Group> groups =
        groupRepository.findAll().stream()
            .filter(group -> group.getTests().stream().anyMatch(t -> t.getId() == testId))
            .toList();

    List<Attempt> allAttempts = attemptRepository.findAll();
    return dataProcessor.processGroupsForTestAttempts(groups, testId, allAttempts, searchQuery);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "studentAttemptsForTeacher",
      key =
          "#principal.name + ':username:' + #username + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
  public StudentTestAttemptsResponseDTO getStudentAttemptsForTeacher(
      Principal principal, String username, String searchQuery) {
    log.info(
        "Teacher {} requesting attempts for student with username {}, search query: {}",
        principal.getName(),
        username,
        searchQuery);

    User teacher = accessValidator.getUserFromPrincipal(principal);
    accessValidator.verifyTeacherRole(teacher);

    User student =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> ResourceNotFoundException.user(username));

    List<Group> teacherGroups =
        groupRepository.findByTeacher(teacher).stream()
            .filter(group -> group.getStudents().contains(student))
            .toList();

    List<Attempt> allAttempts = new ArrayList<>();
    for (Group group : teacherGroups) {
      allAttempts.addAll(
          attemptRepository.findAll().stream()
              .filter(
                  attempt ->
                      attempt.getStudent().equals(student)
                          && group.getStudents().contains(attempt.getStudent()))
              .toList());
    }

    return dataProcessor.processAttemptsForStudent(allAttempts, searchQuery);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "studentAttemptsForAdmin",
      key =
          "#principal.name + ':username:' + #username + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
  public StudentTestAttemptsResponseDTO getStudentAttemptsForAdmin(
      Principal principal, String username, String searchQuery) {
    log.info(
        "Admin {} requesting attempts for student with username {}, search query: {}",
        principal.getName(),
        username,
        searchQuery);

    User admin = accessValidator.getUserFromPrincipal(principal);
    accessValidator.verifyAdminRole(admin);

    User student =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> ResourceNotFoundException.user(username));

    List<Attempt> allAttempts =
        attemptRepository.findAll().stream()
            .filter(attempt -> attempt.getStudent().equals(student))
            .toList();

    return dataProcessor.processAttemptsForStudent(allAttempts, searchQuery);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "attemptReview",
      key = "'admin-teacher:' + #principal.name + ':attemptId:' + #attemptId")
  public AttemptReviewDTO getAttemptReview(Principal principal, Long attemptId) {
    log.info("{} requesting review for attempt {}", principal.getName(), attemptId);

    User user = accessValidator.getUserFromPrincipal(principal);
    Attempt attempt =
        attemptRepository
            .findById(attemptId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

    accessValidator.verifyAttemptAccessPermission(user, attempt);

    return reviewService.createAttemptReviewDTO(attempt);
  }

  @Override
  @Transactional
  public void submitAttemptReview(
      Principal principal, Long attemptId, AttemptReviewSubmissionDTO reviewSubmission) {
    log.info("{} submitting review for attempt {}", principal.getName(), attemptId);

    User user = accessValidator.getUserFromPrincipal(principal);
    Attempt attempt =
        attemptRepository
            .findById(attemptId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Attempt", attemptId.toString(), null));

    accessValidator.verifyAttemptAccessPermission(user, attempt);

    reviewService.processAttemptReviewSubmission(user, attempt, reviewSubmission);
  }
}
