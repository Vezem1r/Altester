package com.altester.core.service.AdminPage;

import com.altester.core.dtos.AdminPage.AdminPageDTO;
import com.altester.core.dtos.AdminPage.UpdateUser;
import com.altester.core.dtos.AdminPage.UsersListDTO;
import com.altester.core.exception.AccessDeniedException;
import com.altester.core.exception.ResourceAlreadyExistsException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.StateConflictException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPageService {

    private static final int PAGE_SIZE = 20;
    private static final String LDAP_FILTER = "ldap";
    private static final String REGISTERED_FILTER = "registered";

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Retrieves a User entity by username or throws a ResourceNotFoundException
     * @param username Username of the user to retrieve
     * @return User entity
     * @throws ResourceNotFoundException if user with given username doesn't exist
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Username {} not found", username);
                    return ResourceNotFoundException.user(username);
                });
    }

    /**
     * Returns a paginated list of students with filtering options
     * @param page Page number (zero-based)
     * @param searchQuery Optional search text to filter students
     * @param searchField Field to search in ("all", "name", "firstName", "lastName", "email", "username")
     * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
     * @return Paginated list of UsersListDTO objects
     */
    public Page<UsersListDTO> getStudents(int page, String searchQuery, String searchField, String registrationFilter) {
        Specification<User> spec = createUserSpecification(RolesEnum.STUDENT, searchQuery, searchField, registrationFilter);

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "isRegistered")
                .and(Sort.by(Sort.Direction.ASC, "name")));

        return userRepository.findAll(spec, pageRequest).map(this::convertToUsersListDTO);
    }

    /**
     * Returns a paginated list of teachers with filtering options
     * @param page Page number (zero-based)
     * @param searchQuery Optional search text to filter teachers
     * @param searchField Field to search in ("all", "name", "firstName", "lastName", "email", "username")
     * @param registrationFilter Filter by registration status ("ldap", "registered", or null for all)
     * @return Paginated list of UsersListDTO objects
     */
    public Page<UsersListDTO> getTeachers(int page, String searchQuery, String searchField, String registrationFilter) {
        Specification<User> spec = createUserSpecification(RolesEnum.TEACHER, searchQuery, searchField, registrationFilter);

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "isRegistered")
                .and(Sort.by(Sort.Direction.ASC, "name")));

        return userRepository.findAll(spec, pageRequest).map(this::convertToUsersListDTO);
    }

    /**
     * Creates a specification for filtering users based on role and search criteria
     * @param role Role to filter by
     * @param searchQuery Search text to filter users
     * @param searchField Field to search in
     * @param registrationFilter Filter by registration status
     * @return Specification for user filtering
     */
    private Specification<User> createUserSpecification(RolesEnum role, String searchQuery, String searchField, String registrationFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("role"), role));

            if (LDAP_FILTER.equals(registrationFilter)) {
                predicates.add(criteriaBuilder.equal(root.get("isRegistered"), false));
            } else if (REGISTERED_FILTER.equals(registrationFilter)) {
                predicates.add(criteriaBuilder.equal(root.get("isRegistered"), true));
            }

            if (StringUtils.hasText(searchQuery)) {
                String likePattern = "%" + searchQuery.toLowerCase() + "%";

                if ("all".equals(searchField)) {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), likePattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern)
                    ));
                } else if ("name".equals(searchField)) {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), likePattern)
                    ));
                } else if ("firstName".equals(searchField)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern));
                } else if ("lastName".equals(searchField)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), likePattern));
                } else if ("email".equals(searchField)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern));
                } else if ("username".equals(searchField)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Retrieves admin page data with system statistics
     * @param username Username of the admin user
     * @return AdminPageDTO with system statistics
     * @throws ResourceNotFoundException if user with given username doesn't exist
     */
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
        return dto;
    }

    /**
     * Changes a teacher's role to student and removes them from teaching responsibilities
     * @param username Username of the teacher to demote
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws StateConflictException if user is already a student
     */
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

        log.info("User {} (ID: {}) successfully promoted to STUDENT", user.getUsername(), user.getId());
    }

    /**
     * Changes a student's role to teacher and removes them from student groups
     * @param username Username of the student to promote
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws StateConflictException if user is already a teacher
     */
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

        log.info("User {} (ID: {}) successfully promoted to TEACHER", user.getUsername(), user.getId());
    }

    /**
     * Updates a user's profile information
     * @param updateUser DTO containing updated user information
     * @param username Username of the user to update
     * @return Updated UsersListDTO
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws AccessDeniedException if trying to update an LDAP user
     * @throws ResourceAlreadyExistsException if new username is already taken
     */
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

        log.info("User {} (ID: {}) successfully updated", savedUser.getUsername(), savedUser.getId());
        return convertToUsersListDTO(savedUser);
    }

    /**
     * Converts a User entity to UsersListDTO with group and subject associations
     * @param user User entity to convert
     * @return UsersListDTO with user information and associations
     */
    private UsersListDTO convertToUsersListDTO(User user) {
        UsersListDTO dto = new UsersListDTO();
        dto.setFirstName(user.getName());
        dto.setLastName(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setLastLogin(user.getLastLogin());
        dto.setRegistered(user.isRegistered());

        List<Group> userGroups;
        if (user.getRole() == RolesEnum.STUDENT) {
            userGroups = groupRepository.findAllByStudentsContaining(user);
        } else if (user.getRole() == RolesEnum.TEACHER) {
            userGroups = groupRepository.findAllByTeacher(user);
        } else {
            userGroups = List.of();
        }

        List<Group> activeUserGroups = userGroups.stream()
                .filter(Group::isActive)
                .toList();

        dto.setGroupNames(activeUserGroups.stream()
                .map(Group::getName)
                .toList());

        List<String> subjectNames = activeUserGroups.stream()
                .map(group -> subjectRepository.findByGroupsId(group.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Subject::getShortName)
                .distinct()
                .toList();

        dto.setSubjectShortNames(subjectNames);

        return dto;
    }
}