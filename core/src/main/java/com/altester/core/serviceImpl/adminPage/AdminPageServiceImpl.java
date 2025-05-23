package com.altester.core.serviceImpl.adminPage;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.repository.*;
import com.altester.core.service.AdminPageService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.AiAccuracy;
import com.altester.core.util.CacheablePage;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
  private final AiAccuracy aiAccuracy;

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

    AdminPageDTO dto = new AdminPageDTO();
    dto.setStudentsCount(userRepository.countByRole(RolesEnum.STUDENT));
    dto.setTeachersCount(userRepository.countByRole(RolesEnum.TEACHER));
    dto.setGroupsCount(groupRepository.count());
    dto.setSubjectsCount(subjectRepository.count());
    dto.setTestsCount(testRepository.count());
    dto.setUsername(username);
    dto.setAiAccuracy(aiAccuracy.calculateAiAccuracy());
    return dto;
  }
}
