package com.altester.core.service.AdminPage;

import com.altester.core.dtos.AdminPage.AdminPageDTO;
import com.altester.core.dtos.AdminPage.UpdateUser;
import com.altester.core.dtos.AdminPage.UsersListDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPageService {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;

    public Page<UsersListDTO> getStudents(int page) {
        try {
            return userRepository.findByRole(RolesEnum.STUDENT, PageRequest.of(page, 20,
                            Sort.by(Sort.Direction.ASC, "isRegistered")))
                    .map(this::convertToUsersListDTO);
        } catch (Exception e) {
            log.error("Error fetching students: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch students. " + e.getMessage());
        }
    }

    public Page<UsersListDTO> getTeachers(int page) {
        try {
            return userRepository.findByRole(RolesEnum.TEACHER, PageRequest.of(page, 20,
                            Sort.by(Sort.Direction.ASC, "isRegistered")))
                    .map(this::convertToUsersListDTO);
        } catch (Exception e) {
            log.error("Error fetching teachers: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch teachers. " + e.getMessage());
        }
    }

    private UsersListDTO convertToUsersListDTO(User user) {
        UsersListDTO dto = new UsersListDTO();
        dto.setFirstName(user.getName());
        dto.setLastName(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setLastLogin(user.getLastLogin());
        dto.setRegistered(user.isRegistered());
        return dto;
    }

    public AdminPageDTO getPage(String username) {
        try {
            userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Teacher not found"));

            AdminPageDTO dto = new AdminPageDTO();
            dto.setStudentsCount(userRepository.countByRole(RolesEnum.STUDENT));
            dto.setTeachersCount(userRepository.countByRole(RolesEnum.TEACHER));
            dto.setGroupsCount(groupRepository.count());
            dto.setSubjectsCount(subjectRepository.count());
            dto.setTestsCount(testRepository.count());
            dto.setUsername(username);
            return dto;
        } catch (Exception e) {
            log.error("Error fetching admin page stats: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch admin page stats. " + e.getMessage());
        }
    }

    public void promoteStudent(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("Username {} not found", username);
                return new RuntimeException("User " + username + " not found");
            });

            if (user.getRole() == RolesEnum.STUDENT) {
                log.warn("User {} is already a student", user.getUsername());
                throw new RuntimeException("User is already a student");
            }

            user.setRole(RolesEnum.STUDENT);
            userRepository.save(user);

            log.info("User {} (ID: {}) successfully promoted to STUDENT", user.getUsername(), user.getId());
        } catch (Exception e) {
            log.error("Failed to promote user to TEACHER: {}", e.getMessage());
            throw new RuntimeException("Failed to promote user to STUDENT. " + e.getMessage());
        }
    }

    public void promoteTeacher(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("Username {} not found", username);
                return new RuntimeException("User " + username + " not found");
            });

            if (user.getRole() == RolesEnum.TEACHER) {
                log.warn("User {} is already a teacher", user.getUsername());
                throw new RuntimeException("User is already a teacher");
            }

            user.setRole(RolesEnum.TEACHER);
            userRepository.save(user);

            log.info("User {} (ID: {}) successfully promoted to TEACHER", user.getUsername(), user.getId());
        } catch (Exception e) {
            log.error("Failed to promote user to TEACHER: {}", e.getMessage());
            throw new RuntimeException("Failed to promote user to TEACHER. " + e.getMessage());
        }
    }

    public UsersListDTO updateUser(UpdateUser updateUser, String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("Username {} not found", username);
                return new RuntimeException("User " + username + " not found");
            });

            if (!user.isRegistered()) {
                log.warn("User {} was created via LDAP and cannot be updated", user.getUsername());
                throw new RuntimeException("User was created via LDAP");
            }

            Optional<User> optionalUser = userRepository.findByUsername(updateUser.getUsername());
            if (optionalUser.isPresent()) {
                log.error("User with username {} already exists", user.getUsername());
                throw new RuntimeException("User with username already exists");
            }

            user.setName(updateUser.getName());
            user.setSurname(updateUser.getLastname());
            user.setEmail(updateUser.getEmail());
            user.setUsername(updateUser.getUsername());

            userRepository.save(user);

            log.info("User {} (ID: {}) successfully updated", user.getUsername(), user.getId());
            return convertToUsersListDTO(user);
        } catch (Exception e) {
            log.error("Failed to update user with username {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to update user. "+ e.getMessage());
        }
    }
}
