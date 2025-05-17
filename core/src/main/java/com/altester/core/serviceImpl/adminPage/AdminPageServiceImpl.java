package com.altester.core.serviceImpl.adminPage;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UpdateUser;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceAlreadyExistsException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Group;
import com.altester.core.repository.*;
import com.altester.core.service.AdminPageService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.CacheablePage;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPageServiceImpl implements AdminPageService {

  private static final int PAGE_SIZE = 20;
  private static final String LDAP_FILTER = "ldap";
  private static final String REGISTERED_FILTER = "registered";

  private static final String USERNAME_FLAG = "username";
  private static final String REGISTERED_FLAG = "isRegistered";
  private static final String EMAIL_FLAG = "email";
  private static final String SURNAME_FLAG = "surname";
  private static final String NAME_FLAG = "name";

  private final UserRepository userRepository;
  private final TestRepository testRepository;
  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final UserMapper userMapper;
  private final CacheService cacheService;
  private final AttemptRepository attemptRepository;

  private User getUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> {
              log.error("Username {} not found", username);
              return ResourceNotFoundException.user(username);
            });
  }

  @Override
  @Cacheable(
      value = "students",
      key =
          "'page:' + #page +"
              + "':search:' + (#searchQuery == null ? '' : #searchQuery) +"
              + "':field:' + #searchField +"
              + "':filter:' + #registrationFilter")
  public CacheablePage<UsersListDTO> getStudents(
      int page, String searchQuery, String searchField, String registrationFilter) {
    log.debug(
        "Getting students: page={}, searchQuery={}, searchField={}, filter={}",
        page,
        searchQuery,
        searchField,
        registrationFilter);

    Specification<User> spec =
        createUserSpecification(RolesEnum.STUDENT, searchQuery, searchField, registrationFilter);

    PageRequest pageRequest =
        PageRequest.of(
            page,
            PAGE_SIZE,
            Sort.by(Sort.Direction.ASC, REGISTERED_FLAG)
                .and(Sort.by(Sort.Direction.ASC, NAME_FLAG)));

    Page<UsersListDTO> result =
        userRepository.findAll(spec, pageRequest).map(userMapper::convertToUsersListDTO);
    return new CacheablePage<>(result);
  }

  @Override
  @Cacheable(
      value = "teachers",
      key =
          "'page:' + #page +"
              + "':search:' + (#searchQuery == null ? '' : #searchQuery) +"
              + "':field:' + #searchField +"
              + "':filter:' + #registrationFilter")
  public CacheablePage<UsersListDTO> getTeachers(
      int page, String searchQuery, String searchField, String registrationFilter) {
    log.debug(
        "Getting teachers: page={}, searchQuery={}, searchField={}, filter={}",
        page,
        searchQuery,
        searchField,
        registrationFilter);
    Specification<User> spec =
        createUserSpecification(RolesEnum.TEACHER, searchQuery, searchField, registrationFilter);

    PageRequest pageRequest =
        PageRequest.of(
            page,
            PAGE_SIZE,
            Sort.by(Sort.Direction.ASC, REGISTERED_FLAG)
                .and(Sort.by(Sort.Direction.ASC, NAME_FLAG)));

    Page<UsersListDTO> result =
        userRepository.findAll(spec, pageRequest).map(userMapper::convertToUsersListDTO);
    return new CacheablePage<>(result);
  }

  private Specification<User> createUserSpecification(
      RolesEnum role, String searchQuery, String searchField, String registrationFilter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get("role"), role));

      if (LDAP_FILTER.equals(registrationFilter)) {
        predicates.add(criteriaBuilder.equal(root.get(REGISTERED_FLAG), false));
      } else if (REGISTERED_FILTER.equals(registrationFilter)) {
        predicates.add(criteriaBuilder.equal(root.get(REGISTERED_FLAG), true));
      }

      if (StringUtils.hasText(searchQuery)) {
        String likePattern = "%" + searchQuery.toLowerCase() + "%";

        if ("all".equals(searchField)) {
          predicates.add(
              criteriaBuilder.or(
                  criteriaBuilder.like(criteriaBuilder.lower(root.get(NAME_FLAG)), likePattern),
                  criteriaBuilder.like(criteriaBuilder.lower(root.get(SURNAME_FLAG)), likePattern),
                  criteriaBuilder.like(criteriaBuilder.lower(root.get(EMAIL_FLAG)), likePattern),
                  criteriaBuilder.like(
                      criteriaBuilder.lower(root.get(USERNAME_FLAG)), likePattern)));
        } else if ("name".equals(searchField)) {
          predicates.add(
              criteriaBuilder.or(
                  criteriaBuilder.like(criteriaBuilder.lower(root.get(NAME_FLAG)), likePattern),
                  criteriaBuilder.like(
                      criteriaBuilder.lower(root.get(SURNAME_FLAG)), likePattern)));
        } else if ("firstName".equals(searchField)) {
          predicates.add(
              criteriaBuilder.like(criteriaBuilder.lower(root.get(NAME_FLAG)), likePattern));
        } else if ("lastName".equals(searchField)) {
          predicates.add(
              criteriaBuilder.like(criteriaBuilder.lower(root.get(SURNAME_FLAG)), likePattern));
        } else if (EMAIL_FLAG.equals(searchField)) {
          predicates.add(
              criteriaBuilder.like(criteriaBuilder.lower(root.get(EMAIL_FLAG)), likePattern));
        } else if (USERNAME_FLAG.equals(searchField)) {
          predicates.add(
              criteriaBuilder.like(criteriaBuilder.lower(root.get(USERNAME_FLAG)), likePattern));
        }
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  @Override
  @Cacheable(value = "adminStats", key = "#username")
  public AdminPageDTO getPage(String username) {
    log.debug("Fetching admin page data for user: {}", username);
    getUserByUsername(username);

    AdminPageDTO dto = new AdminPageDTO();
    dto.setStudentsCount(userRepository.countByRole(RolesEnum.STUDENT));
    dto.setTeachersCount(userRepository.countByRole(RolesEnum.TEACHER));
    dto.setGroupsCount(groupRepository.count());
    dto.setSubjectsCount(subjectRepository.count());
    dto.setTestsCount(testRepository.count());
    dto.setUsername(username);
    dto.setAiAccuracy(calculateAiAccuracy());
    return dto;
  }

  @Override
  @Transactional
  public void demoteToStudent(String username) {
    User user = getUserByUsername(username);

    if (user.getRole() == RolesEnum.STUDENT) {
      log.warn("User {} is already a student", user.getUsername());
      throw StateConflictException.roleConflict("User is already a student");
    }

    List<Group> teacherGroups = groupRepository.findAllByTeacher(user);

    if (!teacherGroups.isEmpty()) {
      teacherGroups.forEach(group -> group.setTeacher(null));
      groupRepository.saveAll(teacherGroups);
      log.info("Removed user {} from {} teacher roles", username, teacherGroups.size());
    }

    user.setRole(RolesEnum.STUDENT);
    userRepository.save(user);

    cacheService.clearAdminRelatedCaches();
    cacheService.clearStudentRelatedCaches();
    cacheService.clearTeacherRelatedCaches();

    log.info("User {} (ID: {}) successfully promoted to STUDENT", user.getUsername(), user.getId());
  }

  @Override
  @Transactional
  public void promoteToTeacher(String username) {
    User user = getUserByUsername(username);

    if (user.getRole() == RolesEnum.TEACHER) {
      log.warn("User {} is already a teacher", user.getUsername());
      throw StateConflictException.roleConflict("User is already a teacher");
    }

    List<Group> studentGroups = groupRepository.findAllByStudentsContaining(user);

    if (!studentGroups.isEmpty()) {
      studentGroups.forEach(group -> group.getStudents().remove(user));
      groupRepository.saveAll(studentGroups);
      log.info("Removed user {} from {} student groups", username, studentGroups.size());
    }

    user.setRole(RolesEnum.TEACHER);
    userRepository.save(user);

    cacheService.clearAdminRelatedCaches();
    cacheService.clearTeacherRelatedCaches();
    cacheService.clearStudentRelatedCaches();

    log.info("User {} (ID: {}) successfully promoted to TEACHER", user.getUsername(), user.getId());
  }

  @Override
  @Transactional
  public UsersListDTO updateUser(UpdateUser updateUser, String username) {

    User user = getUserByUsername(username);

    if (!user.isRegistered()) {
      log.warn("User {} was created via LDAP and cannot be updated", user.getUsername());
      throw AccessDeniedException.ldapUserModification();
    }

    if (!user.getUsername().equals(updateUser.getUsername())) {
      Optional<User> optionalUser = userRepository.findByUsername(updateUser.getUsername());
      if (optionalUser.isPresent()) {
        log.error("User with username {} already exists", updateUser.getUsername());
        throw ResourceAlreadyExistsException.user(updateUser.getUsername());
      }
    }

    user.setName(updateUser.getName());
    user.setSurname(updateUser.getLastname());
    user.setEmail(updateUser.getEmail());
    user.setUsername(updateUser.getUsername());

    User savedUser = userRepository.save(user);

    cacheService.clearAdminRelatedCaches();
    cacheService.clearStudentRelatedCaches();
    cacheService.clearTeacherRelatedCaches();

    log.info("User {} (ID: {}) successfully updated", savedUser.getUsername(), savedUser.getId());
    return userMapper.convertToUsersListDTO(savedUser);
  }

  /**
   * Calculates the AI grading accuracy percentage based on the match between teacher scores and AI
   * scores for all attempts where both scores exist.
   *
   * @return The accuracy percentage rounded to 1 decimal place
   */
  private double calculateAiAccuracy() {
    List<Attempt> attempts = attemptRepository.findAllWithBothScores();

    if (attempts.isEmpty()) {
      return 0.0;
    }

    int totalDiff = 0;
    int totalMaxPossibleDiff = 0;

    for (Attempt attempt : attempts) {
      int diff = Math.abs(attempt.getScore() - attempt.getAiScore());
      int maxPossibleDiff = attempt.getTest().getTotalScore();

      if (maxPossibleDiff <= 0) {
        continue;
      }

      totalDiff += diff;
      totalMaxPossibleDiff += maxPossibleDiff;
    }

    if (totalMaxPossibleDiff == 0) {
      return 0.0;
    }
    double accuracyPercentage = 100.0 - ((double) totalDiff / totalMaxPossibleDiff * 100.0);

    BigDecimal bd = BigDecimal.valueOf(accuracyPercentage);
    bd = bd.setScale(1, RoundingMode.HALF_UP);

    return bd.doubleValue();
  }
}
